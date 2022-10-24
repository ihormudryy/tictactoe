package challenge.utils;

import challenge.tictactoe.db.MoveEntity;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static org.springframework.http.MediaType.APPLICATION_JSON;

public class RestApiUtils {

    private final String API_ENDPOINT = "/api/v1/tictactoe";

    private final WebTestClient client;

    public RestApiUtils(WebTestClient client) {
        this.client = client;
    }

    public WebTestClient.BodyContentSpec deleteGame(String gameId, HttpStatus expectedStatus) {
        return client.delete()
                .uri(uriBuilder -> uriBuilder
                        .path(API_ENDPOINT)
                        .queryParam("gameId", gameId)
                        .build())
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }

    public WebTestClient.BodyContentSpec getAndVerify(String gameId, HttpStatus expectedStatus) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(API_ENDPOINT)
                        .queryParam("gameId", gameId)
                        .build())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    public WebTestClient.BodyContentSpec createNewGameWithAi(HttpStatus expectedStatus) {
        return client.get()
                .uri(API_ENDPOINT + "/ai")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    public WebTestClient.BodyContentSpec createNewGameWithPerson(HttpStatus expectedStatus) {
        return client.get()
                .uri(API_ENDPOINT + "/person")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    public WebTestClient.BodyContentSpec makeMoveWithoutExpectedStatus(String gameId, MoveEntity move) {
        return client.post()
                .uri(uriBuilder -> uriBuilder
                        .path(API_ENDPOINT)
                        .queryParam("gameId", gameId)
                        .build())
                .accept(APPLICATION_JSON)
                .body(BodyInserters.fromValue(move))
                .exchange()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    public WebTestClient.BodyContentSpec makeMove(String gameId, MoveEntity move, HttpStatus expectedStatus) {
        return client.post()
                .uri(uriBuilder -> uriBuilder
                        .path(API_ENDPOINT)
                        .queryParam("gameId", gameId)
                        .build())
                .accept(APPLICATION_JSON)
                .body(BodyInserters.fromValue(move))
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }
}
