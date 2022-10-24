package challenge.tictactoe.api;

import challenge.tictactoe.constant.GameStatus;
import challenge.tictactoe.constant.GameType;
import challenge.tictactoe.constant.GameWinner;
import challenge.tictactoe.db.MoveEntity;
import challenge.utils.RestApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @BeforeEach
    public void init() {
        restApiUtils = new RestApiUtils(client);
    }

    @Test
    void makeMoveInExecutorServiceTest() {
        int numOfExecutors = 10;
        ExecutorService service = Executors.newFixedThreadPool(numOfExecutors);
        restApiUtils.createNewGameWithAi(OK)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo(GameStatus.CREATED)
                .jsonPath("$.winner").isEqualTo(GameWinner.NOT_DEFINED_YET)
                .jsonPath("$.activeTurn").isEqualTo(GameWinner.PLAYER)
                .jsonPath("$.gameType").isEqualTo(GameType.AGAINST_AI)
                .jsonPath("$.id").value(id -> {
                    for (int i = 0; i < numOfExecutors; i++) {
                        service.execute(new Runnable() {
                            public void run() {
                                restApiUtils.makeMoveWithoutExpectedStatus(id.toString(),
                                                MoveEntity.builder()
                                                        .gameId(id.toString())
                                                        .x(1)
                                                        .y(1)
                                                        .number(1)
                                                        .playedBy(GameWinner.PLAYER)
                                                        .build())
                                        .jsonPath("$.id").isNotEmpty()
                                        .jsonPath("$.status").isNotEmpty();
                                log.info("Thread " + Thread.currentThread().getName() + " has been executed");
                            }
                        });
                    }
                    service.shutdown();
                });

    }
}
