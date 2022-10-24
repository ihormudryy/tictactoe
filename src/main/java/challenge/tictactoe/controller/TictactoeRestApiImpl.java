package challenge.tictactoe.controller;

import challenge.tictactoe.api.TictactoeRestApi;
import challenge.tictactoe.constant.GameType;
import challenge.tictactoe.constant.GameWinner;
import challenge.tictactoe.dto.GameDto;
import challenge.tictactoe.dto.MoveDto;
import challenge.tictactoe.service.TictactoeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class TictactoeRestApiImpl implements TictactoeRestApi {

    private final TictactoeService tictactoeService;

    @Autowired
    public TictactoeRestApiImpl(TictactoeService tictactoeService) {
        this.tictactoeService = tictactoeService;
    }

    /**
     * Call Tictactoe service to create a next move and validate game results
     *
     * @param gameId String
     * @param body   MoveDto
     * @return GameDto
     */
    @Override
    public Mono<GameDto> makeMove(String gameId, MoveDto body) {
        return tictactoeService.makeTictactoeMove(gameId, body);
    }

    /**
     * Create new Tictactoe game between two persons: player_1 and player_2
     *
     * @return GameDto
     */
    @Override
    public Mono<GameDto> createNewGameWithTwoPlayers() {

        return tictactoeService.createNewGame(GameWinner.PLAYER_1, GameType.AGAINST_HUMAN);
    }

    /**
     * Create new Tictactoe game between person and AI
     *
     * @return Mono<GameEntity>
     */
    @Override
    public Mono<GameDto> createNewGameWithAi() {
        return tictactoeService.createNewGame(GameWinner.PLAYER, GameType.AGAINST_AI);
    }

    /**
     * Fetch game object from DB
     *
     * @param gameId String
     * @return GameDto
     */
    @Override
    public Mono<GameDto> getGameObject(String gameId) {
        return tictactoeService.getGame(gameId);
    }

    /**
     * Delete game from DB
     *
     * @param gameId String
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> deleteGame(String gameId) {
        return tictactoeService.deleteGame(gameId);
    }
}
