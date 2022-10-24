package challenge.tictactoe.api;

import challenge.tictactoe.dto.GameDto;
import challenge.tictactoe.dto.MoveDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Api(value = "REST API v1 for Tictactoe challenge", tags = {"description"})
@RequestMapping("/api/v1/tictactoe")
public interface TictactoeRestApi {

    /**
     * Creates a new game between person and AI
     * <p>
     * Sample usage:
     * <p>
     * curl -X GET $HOST:$PORT/api/v1/tictactoe/ai
     *
     * @return body
     */
    @ApiOperation(
            value = "${api.tictactoe.get-new-game.description}",
            notes = "${api.tictactoe.get-new-game.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Game was created.")
    })
    @GetMapping(
            value = "/ai",
            produces = "application/json")
    Mono<GameDto> createNewGameWithAi();

    /**
     * Creates a new game between two persons
     * <p>
     * Sample usage:
     * <p>
     * curl -X GET $HOST:$PORT/api/v1/tictactoe/person
     *
     * @return body
     */
    @ApiOperation(
            value = "${api.tictactoe.get-new-game.description}",
            notes = "${api.tictactoe.get-new-game.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Game was successfully created.")
    })
    @GetMapping(
            value = "/person",
            produces = "application/json")
    Mono<GameDto> createNewGameWithTwoPlayers();

    /**
     * Make a Tictactoe move in existing game on 3x3 board
     * <p>
     * Sample usage:
     * <p>
     * curl -X POST $HOST:$PORT/api/v1/tictactoe?gameId=6356705f775641388748dd46 \
     * -H "Content-Type: application/json" --data \
     * '{"x":1, "y":1}'
     *
     * @param gameId URL encoded parameter of Game ID
     * @return
     */
    @ApiOperation(
            value = "${api.tictactoe.make-a-move.description}",
            notes = "${api.tictactoe.make-a-move.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Move was processed and added."),
            @ApiResponse(code = 400,
                    message = "Bad Request, invalid format of the request. " +
                            "See response message for more information."),
            @ApiResponse(code = 404, message = "Game doesn't exists.")
    })
    @PostMapping(consumes = "application/json")
    Mono<GameDto> makeMove(@RequestParam String gameId, @RequestBody MoveDto body);

    /**
     * Get existing Tictactoe game object
     * <p>
     * Sample usage:
     * <p>
     * curl -X GET $HOST:$PORT/api/v1/tictactoe?gameId=6356705f775641388748dd46
     *
     * @param gameId URL encoded parameter of Game ID
     * @return
     */
    @ApiOperation(
            value = "${api.tictactoe.get-game-object.description}",
            notes = "${api.tictactoe.get-game-object.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Game was found."),
            @ApiResponse(code = 404, message = "Game doesn't exists.")
    })
    @GetMapping(produces = "application/json")
    Mono<GameDto> getGameObject(@RequestParam String gameId);

    /**
     * Delete existing Tictactoe game object
     * <p>
     * Sample usage:
     * <p>
     * curl -X DELETE $HOST:$PORT/api/v1/tictactoe?gameId=6356705f775641388748dd46
     *
     * @param gameId URL encoded parameter of Game ID
     */
    @ApiOperation(
            value = "${api.tictactoe.delete-game-object.description}",
            notes = "${api.tictactoe.delete-game-object.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation was processed. No information if the actual game was " +
                    "deleted due to security reasons.")
    })
    @DeleteMapping
    Mono<Void> deleteGame(@RequestParam String gameId);
}
