package challenge.tictactoe.api;

import challenge.tictactoe.constant.GameStatus;
import challenge.tictactoe.constant.GameType;
import challenge.tictactoe.constant.GameWinner;
import challenge.tictactoe.db.MoveEntity;
import challenge.tictactoe.persistance.GameRepository;
import challenge.tictactoe.persistance.MoveRepository;
import challenge.utils.RestApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("test")
@Tag("rest-api-test")
@Slf4j
public class ApiConcurrencyTest {

    @Autowired
    WebTestClient client;
    RestApiUtils restApiUtils;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private MoveRepository moveRepository;

    @BeforeEach
    public void init() {

        restApiUtils = new RestApiUtils(client);
        moveRepository.deleteAll().block();
        gameRepository.deleteAll().block();

    }

    @Test
    void makeParallelMovesAgainstPersonTest() {
        List<List<Integer>> moves1 = List.of(
                List.of(1, 1),
                List.of(0, 1),
                List.of(0, 2),
                List.of(1, 0),
                List.of(2, 2));
        List<List<Integer>> moves2 = List.of(
                List.of(0, 0),
                List.of(2, 1),
                List.of(2, 0),
                List.of(1, 2));
        restApiUtils.createNewGameWithPerson(OK)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo(GameStatus.CREATED)
                .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER_1)
                .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_HUMAN)
                .jsonPath("$.id").value(id -> {
                    log.info("Creating service");
                    int processors = Runtime.getRuntime().availableProcessors();
                    ExecutorService service =
                            Executors.newFixedThreadPool(processors);
                    log.info("Starting on {} processors", processors);

                    for (int i = 0; i < 4; i++) {

                        List<RunInParallelGameAgainstPerson> futureListPLayer1 = new ArrayList<>();
                        for (int jj = 0; jj < processors; jj++) {
                            futureListPLayer1.add(new RunInParallelGameAgainstPerson(client,
                                    restApiUtils,
                                    gameRepository,
                                    moveRepository,
                                    id.toString(),
                                    i));
                        }
                        log.info("Starting");
                        try {
                            List<Future<Boolean>> futures = service.invokeAll(futureListPLayer1);
                        } catch (Exception err) {
                            log.error(err.getMessage());
                        }
                        log.info("Completed");
                    }
                    service.shutdown();
                    restApiUtils.makeMove(id.toString(),
                                    MoveEntity.builder()
                                            .gameId(id.toString())
                                            .x(moves1.get(4).get(0))
                                            .y(moves1.get(4).get(1))
                                            .number(9)
                                            .playedBy(GameWinner.PLAYER_1)
                                            .build(), OK)
                            .jsonPath("$.id").isEqualTo(id.toString())
                            .jsonPath("$.moves").value(v -> {
                                log.info("Moves: {}", v);
                            })
                            .jsonPath("$.moves.length()").isEqualTo(9)
                            .jsonPath("$.winner").isEqualTo(GameWinner.DRAW)
                            .jsonPath("$.status").isEqualTo(GameStatus.FINISHED)
                            .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_HUMAN)
                            .jsonPath("$.activeTurn").doesNotExist();
                });
    }

    @Test
    void makeMoveInExecutorServiceTest() {
        log.info("Creating service");
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService service =
                Executors.newFixedThreadPool(processors);
        log.info("Starting on {} processors", processors);
        List<RunInParallelGameAgainstAi> futureList = new ArrayList<>();
        for (int i = 0; i < processors; i++) {
            futureList.add(new RunInParallelGameAgainstAi(client,
                    restApiUtils,
                    gameRepository,
                    moveRepository));
        }

        log.info("Start");
        try {
            List<Future<String>> futures = service.invokeAll(futureList);
        } catch (Exception err) {
            err.printStackTrace();
        }
        log.info("Completed");
        service.shutdown();
    }

    /**
     * Verify that concurrent moves would not lead to data inconsistency or DB duplicates
     */
    class RunInParallelGameAgainstPerson implements Callable<Boolean> {

        String id;
        int index;
        WebTestClient client;
        RestApiUtils restApiUtils;
        List<List<Integer>> moves1 = List.of(
                List.of(1, 1),
                List.of(0, 1),
                List.of(0, 2),
                List.of(1, 0),
                List.of(2, 2));
        List<List<Integer>> moves2 = List.of(
                List.of(0, 0),
                List.of(2, 1),
                List.of(2, 0),
                List.of(1, 2));
        private final GameRepository gameRepository;
        private final MoveRepository moveRepository;

        public RunInParallelGameAgainstPerson(WebTestClient client,
                                              RestApiUtils restApiUtils,
                                              GameRepository gameRepository,
                                              MoveRepository moveRepository,
                                              String gameId,
                                              int index) {
            this.client = client;
            this.restApiUtils = restApiUtils;
            this.gameRepository = gameRepository;
            this.moveRepository = moveRepository;
            this.id = gameId;
            this.index = index;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                restApiUtils.makeMoveWithoutExpectedStatus(id, MoveEntity.builder()
                        .gameId(id)
                        .x(moves1.get(index).get(0))
                        .y(moves1.get(index).get(1))
                        .number(index + 1)
                        .playedBy(GameWinner.PLAYER_1)
                        .build());
            } catch (Exception e) {
                log.info("Bad request {}", e.getMessage());
            }
            try {
                restApiUtils.makeMoveWithoutExpectedStatus(id, MoveEntity.builder()
                        .gameId(id)
                        .x(moves2.get(index).get(0))
                        .y(moves2.get(index).get(1))
                        .number(index + 2)
                        .playedBy(GameWinner.PLAYER_2)
                        .build());
            } catch (Exception e) {
                log.info("Bad request {}", e.getMessage());
            }
            return true;
        }
    }

    class RunInParallelGameAgainstAi implements Callable<String> {

        WebTestClient client;
        RestApiUtils restApiUtils;
        private final GameRepository gameRepository;
        private final MoveRepository moveRepository;

        public RunInParallelGameAgainstAi(WebTestClient client,
                                          RestApiUtils restApiUtils,
                                          GameRepository gameRepository,
                                          MoveRepository moveRepository) {
            this.client = client;
            this.restApiUtils = restApiUtils;
            this.gameRepository = gameRepository;
            this.moveRepository = moveRepository;
        }

        public String call() {
            AtomicReference<String> gameId = null;
            restApiUtils.createNewGameWithAi(OK)
                    .jsonPath("$.id").isNotEmpty()
                    .jsonPath("$.status").isEqualTo(GameStatus.CREATED)
                    .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                    .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER)
                    .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_AI)
                    .jsonPath("$.id").value(id -> {
                        List<List<Integer>> moves = List.of(
                                List.of(1, 1),
                                List.of(2, 0),
                                List.of(0, 1),
                                List.of(1, 2));
                        for (int i = 0; i < moves.size(); i++) {
                            MoveEntity entity = MoveEntity.builder()
                                    .gameId(id.toString())
                                    .x(moves.get(i).get(0))
                                    .y(moves.get(i).get(1))
                                    .number(i)
                                    .playedBy(GameWinner.PLAYER)
                                    .build();
                            restApiUtils.makeMove(id.toString(), entity, OK)
                                    .jsonPath("$.id").isEqualTo(id.toString())
                                    .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                                    .jsonPath("$.status").isEqualTo(GameStatus.IN_PROGRESS)
                                    .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER)
                                    .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_AI)
                                    .jsonPath("$.moves.length()").isEqualTo((i + 1) * 2);
                        }
                        MoveEntity entity = MoveEntity.builder()
                                .gameId(id.toString())
                                .x(2)
                                .y(2)
                                .number(9)
                                .playedBy(GameWinner.PLAYER)
                                .build();
                        restApiUtils.makeMove(id.toString(), entity, OK)
                                .jsonPath("$.id").isEqualTo(id.toString())
                                .jsonPath("$.winner").isEqualTo(GameWinner.DRAW)
                                .jsonPath("$.status").isEqualTo(GameStatus.FINISHED)
                                .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_AI)
                                .jsonPath("$.activeTurn").doesNotExist()
                                .jsonPath("$.moves.length()").isEqualTo(9);
                        log.info("Game ID: " + id + " finished successfully ");
                        gameId.set(id.toString());
                    });
            return gameId.get();
        }
    }
}
