package challenge.tictactoe.api;

import challenge.tictactoe.constant.GameStatus;
import challenge.tictactoe.constant.GameType;
import challenge.tictactoe.constant.GameWinner;
import challenge.tictactoe.db.MoveEntity;
import challenge.tictactoe.persistance.GameRepository;
import challenge.tictactoe.persistance.MoveRepository;
import challenge.utils.RestApiUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("test")
@Tag("rest-api-test")
public class TictactoeRestApiWithHumanTest {

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
        gameRepository.deleteAll().block();
        moveRepository.deleteAll().block();
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    void testFullLostGamePersonAgainstPerson() {
        List<List<Integer>> moves1 = List.of(
                List.of(1, 1),
                List.of(1, 0),
                List.of(1, 2));
        List<List<Integer>> moves2 = List.of(
                List.of(0, 0),
                List.of(0, 1));
        restApiUtils.createNewGameWithPerson(OK)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo(GameStatus.CREATED)
                .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER_1)
                .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_HUMAN)
                .jsonPath("$.id").value(id -> {
                    for (int i = 0; i < moves2.size(); i++) {
                        restApiUtils.makeMove(id.toString(),
                                        MoveEntity.builder()
                                                .gameId(id.toString())
                                                .x(moves1.get(i).get(0))
                                                .y(moves1.get(i).get(1))
                                                .number(i + 1)
                                                .playedBy(GameWinner.PLAYER_1)
                                                .build(), OK)
                                .jsonPath("$.id").isEqualTo(id.toString())
                                .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                                .jsonPath("$.status").isEqualTo(GameStatus.IN_PROGRESS)
                                .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER_2)
                                .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_HUMAN)
                                .jsonPath("$.moves.length()").isEqualTo(i * 2 + 1);
                        restApiUtils.makeMove(id.toString(),
                                        MoveEntity.builder()
                                                .gameId(id.toString())
                                                .x(moves2.get(i).get(0))
                                                .y(moves2.get(i).get(1))
                                                .number(i + 2)
                                                .playedBy(GameWinner.PLAYER_2)
                                                .build(), OK)
                                .jsonPath("$.id").isEqualTo(id.toString())
                                .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                                .jsonPath("$.status").isEqualTo(GameStatus.IN_PROGRESS)
                                .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER_1)
                                .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_HUMAN)
                                .jsonPath("$.moves.length()").isEqualTo(i * 2 + 2);
                    }
                    restApiUtils.makeMove(id.toString(),
                                    MoveEntity.builder()
                                            .gameId(id.toString())
                                            .x(moves1.get(2).get(0))
                                            .y(moves1.get(2).get(1))
                                            .number(5)
                                            .playedBy(GameWinner.PLAYER_1)
                                            .build(), OK)
                            .jsonPath("$.id").isEqualTo(id.toString())
                            .jsonPath("$.winner").isEqualTo(GameWinner.PLAYER_1)
                            .jsonPath("$.status").isEqualTo(GameStatus.FINISHED)
                            .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_HUMAN)
                            .jsonPath("$.activeTurn").doesNotExist()
                            .jsonPath("$.moves.length()").isEqualTo(5);
                });
    }


    @Test
    void testDrawGamePersonAgainstPerson() {
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

                    for (int i = 0; i < moves2.size(); i++) {
                        restApiUtils.makeMove(id.toString(),
                                        MoveEntity.builder()
                                                .gameId(id.toString())
                                                .x(moves1.get(i).get(0))
                                                .y(moves1.get(i).get(1))
                                                .number(i + 1)
                                                .playedBy(GameWinner.PLAYER_1)
                                                .build(), OK)
                                .jsonPath("$.id").isEqualTo(id.toString())
                                .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                                .jsonPath("$.status").isEqualTo(GameStatus.IN_PROGRESS)
                                .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER_2)
                                .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_HUMAN)
                                .jsonPath("$.moves.length()").isEqualTo(i * 2 + 1);

                        restApiUtils.makeMove(id.toString(),
                                        MoveEntity.builder()
                                                .gameId(id.toString())
                                                .x(moves2.get(i).get(0))
                                                .y(moves2.get(i).get(1))
                                                .number(i + 2)
                                                .playedBy(GameWinner.PLAYER_2)
                                                .build(), OK)
                                .jsonPath("$.id").isEqualTo(id.toString())
                                .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                                .jsonPath("$.status").isEqualTo(GameStatus.IN_PROGRESS)
                                .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER_1)
                                .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_HUMAN)
                                .jsonPath("$.moves.length()").isEqualTo(i * 2 + 2);
                    }
                    restApiUtils.makeMove(id.toString(),
                                    MoveEntity.builder()
                                            .gameId(id.toString())
                                            .x(moves1.get(4).get(0))
                                            .y(moves1.get(4).get(1))
                                            .number(9)
                                            .playedBy(GameWinner.PLAYER_1)
                                            .build(), OK)
                            .jsonPath("$.id").isEqualTo(id.toString())
                            .jsonPath("$.winner").isEqualTo(GameWinner.DRAW)
                            .jsonPath("$.status").isEqualTo(GameStatus.FINISHED)
                            .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_HUMAN)
                            .jsonPath("$.activeTurn").doesNotExist()
                            .jsonPath("$.moves.length()").isEqualTo(9);
                });
    }
}
