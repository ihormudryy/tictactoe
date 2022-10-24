package challenge.tictactoe.api;

import challenge.tictactoe.constant.ErrorMessages;
import challenge.tictactoe.constant.GameStatus;
import challenge.tictactoe.constant.GameType;
import challenge.tictactoe.constant.GameWinner;
import challenge.tictactoe.db.MoveEntity;
import challenge.tictactoe.persistance.GameRepository;
import challenge.tictactoe.persistance.MoveRepository;
import challenge.utils.RestApiUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("test")
@Tag("rest-api-test")
class TictactoeRestApiWithAiTest {

    @Autowired
    WebTestClient client;
    RestApiUtils restApiUtils;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private MoveRepository moveRepository;

    @BeforeEach
    public void clean() {
        restApiUtils = new RestApiUtils(client);
        gameRepository.deleteAll().block();
        moveRepository.deleteAll().block();
    }

    @Test
    void makeMoveInNonExistingGameTest() {
        restApiUtils.makeMove("9999", MoveEntity.builder()
                .gameId("9999")
                .x(1)
                .y(1)
                .number(0)
                .playedBy(GameWinner.PLAYER)
                .build(), HttpStatus.NOT_FOUND);
    }

    @Test
    void makeInvalidMoveTest() {
        restApiUtils.createNewGameWithAi(OK)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo(GameStatus.CREATED)
                .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER)
                .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_AI)
                .jsonPath("$.id").value(id -> {
                    restApiUtils.makeMove(id.toString(),
                                    MoveEntity.builder()
                                            .gameId(id.toString())
                                            .x(3)
                                            .y(0)
                                            .number(0)
                                            .playedBy(GameWinner.PLAYER)
                                            .build(), BAD_REQUEST)
                            .jsonPath("$.message").isEqualTo(ErrorMessages.OUT_OF_RANGE);
                });
    }

    @Test
    void getInvalidGameObjectTest() {
        restApiUtils.getAndVerify("9999", HttpStatus.NOT_FOUND);
    }

    @Test
    void makeValidOutOfRangeMoveTest() {
        restApiUtils.createNewGameWithAi(OK)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo(GameStatus.CREATED)
                .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER)
                .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_AI)
                .jsonPath("$.id").value(id -> {
                    restApiUtils.makeMove(id.toString(),
                                    MoveEntity.builder()
                                            .gameId(id.toString())
                                            .x(1)
                                            .y(1)
                                            .number(0)
                                            .playedBy(GameWinner.PLAYER)
                                            .build(), OK)
                            .jsonPath("$.id").isEqualTo(id.toString())
                            .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                            .jsonPath("$.status").isEqualTo(GameStatus.IN_PROGRESS)
                            .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER)
                            .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_AI)
                            .jsonPath("$.moves.length()").isEqualTo(2);

                });
    }

    @Test
    void makeInvalidDoubleMoveTest() {
        restApiUtils.createNewGameWithAi(OK)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo(GameStatus.CREATED)
                .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER)
                .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_AI)
                .jsonPath("$.id").value(id -> {
                    MoveEntity entity = MoveEntity.builder()
                            .gameId(id.toString())
                            .x(1)
                            .y(1)
                            .number(0)
                            .playedBy(GameWinner.PLAYER)
                            .build();
                    restApiUtils.makeMove(id.toString(), entity, OK)
                            .jsonPath("$.id").isEqualTo(id.toString())
                            .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                            .jsonPath("$.status").isEqualTo(GameStatus.IN_PROGRESS)
                            .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER)
                            .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_AI)
                            .jsonPath("$.moves.length()").isEqualTo(2);
                    restApiUtils.makeMove(id.toString(), entity, BAD_REQUEST)
                            .jsonPath("$.message").isEqualTo(String.format(ErrorMessages.CELL_X_Y_IS_USED,
                                    1, 1));
                });
    }

    @Test
    void getValidGameObjectTest() {
        restApiUtils.createNewGameWithAi(OK)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo(GameStatus.CREATED)
                .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER)
                .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_AI)
                .jsonPath("$.id").value(id -> {
                    restApiUtils.getAndVerify(id.toString(), OK)
                            .jsonPath("$.id").isNotEmpty()
                            .jsonPath("$.status").isEqualTo(GameStatus.CREATED)
                            .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                            .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_AI)
                            .jsonPath("$.moves.length()").isEqualTo(0)
                            .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER);
                });
    }

    @Test
    void deleteValidGameTest() {
        restApiUtils.createNewGameWithAi(OK)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo(GameStatus.CREATED)
                .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER)
                .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_AI)
                .jsonPath("$.id").value(id -> {
                    restApiUtils.deleteGame(id.toString(), OK);
                    assertNull(gameRepository.findById(id.toString()).block());
                    assertEquals(moveRepository.findByGameId(id.toString())
                            .collectList()
                            .block()
                            .size(), 0);
                });
    }

    @Test
    void testFullDrawGameWithAiTest() {
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
                    restApiUtils.deleteGame(id.toString(), OK);
                    assertNull(gameRepository.findById(id.toString()).block());
                    assertEquals(moveRepository.findByGameId(id.toString())
                            .collectList()
                            .block()
                            .size(), 0);
                });
    }

    @Test
    void lostGameWithAiTest() {
        restApiUtils.createNewGameWithAi(OK)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo(GameStatus.CREATED)
                .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER)
                .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_AI)
                .jsonPath("$.id").value(id -> {
                    List<List<Integer>> moves = List.of(
                            List.of(1, 1),
                            List.of(2, 2),
                            List.of(2, 1));
                    for (int i = 0; i < moves.size() - 1; i++) {
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
                            .x(moves.get(2).get(0))
                            .y(moves.get(2).get(1))
                            .number(5)
                            .playedBy(GameWinner.PLAYER)
                            .build();
                    restApiUtils.makeMove(id.toString(), entity, OK)
                            .jsonPath("$.id").isEqualTo(id.toString())
                            .jsonPath("$.winner").isEqualTo(GameWinner.AI)
                            .jsonPath("$.status").isEqualTo(GameStatus.FINISHED)
                            .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_AI)
                            .jsonPath("$.activeTurn").doesNotExist()
                            .jsonPath("$.moves.length()").isEqualTo(6);
                    restApiUtils.deleteGame(id.toString(), OK);
                    assertNull(gameRepository.findById(id.toString()).block());
                    assertEquals(moveRepository.findByGameId(id.toString())
                            .collectList()
                            .block()
                            .size(), 0);
                });
    }
}