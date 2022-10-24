package challenge.tictactoe.service;

import challenge.tictactoe.constant.GameStatus;
import challenge.tictactoe.constant.GameType;
import challenge.tictactoe.constant.GameWinner;
import challenge.tictactoe.db.GameEntity;
import challenge.tictactoe.db.MoveEntity;
import challenge.tictactoe.dto.GameDto;
import challenge.tictactoe.dto.MoveDto;
import challenge.tictactoe.logic.TictactoeGameEngine;
import challenge.tictactoe.mapper.GameMapper;
import challenge.tictactoe.mapper.MoveMapper;
import challenge.tictactoe.persistance.GameRepository;
import challenge.tictactoe.persistance.MoveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static challenge.tictactoe.constant.ErrorMessages.*;
import static java.util.stream.Collectors.toCollection;

@Service
@Slf4j
public class TictactoeService {

    private final MoveRepository moveRepository;
    private final GameRepository gameRepository;
    private final GameMapper gameMapper;
    private final MoveMapper moveMapper;
    private final TictactoeGameEngine engineAgainstAi;
    private final TictactoeGameEngine engineAgainstPerson;

    @Autowired
    public TictactoeService(MoveRepository moveRepository,
                            GameRepository gameRepository,
                            GameMapper gameMapper,
                            MoveMapper moveMapper) {
        this.moveRepository = moveRepository;
        this.gameRepository = gameRepository;
        this.gameMapper = gameMapper;
        this.moveMapper = moveMapper;
        this.engineAgainstAi = new TictactoeGameEngine();
        this.engineAgainstPerson = new TictactoeGameEngine(true);
    }

    public Mono<GameDto> createNewGame(String player, String gameType) {
        return gameRepository.save(
                        GameEntity
                                .builder()
                                .activeTurn(player)
                                .status(GameStatus.CREATED)
                                .winner(GameWinner.NOT_DEFINED_YET)
                                .gameType(gameType)
                                .build())
                .map(gameMapper::dtoToEntity)
                .doOnError(ex -> log.warn("createNewGame failed: {}", ex.toString()))
                .doOnSuccess(e -> log.info("New game with type {} was created", gameType))
                .log();
    }

    public Mono<Void> deleteGame(String gameId) {
        gameRepository.deleteById(gameId).subscribe();
        return moveRepository
                .findByGameId(gameId)
                .flatMap(move -> moveRepository.delete(move))
                .then()
                .doOnSuccess(e -> log.info("Moves from game {} deleted", gameId))
                .log();
    }

    public Mono<GameDto> makeTictactoeMove(String gameId, MoveDto move) {

        if (move.getX() < 0 || move.getX() > 2 || move.getY() < 0 || move.getY() > 2) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, OUT_OF_RANGE));
        }

        return moveRepository
                .findByGameId(gameId)
                .concatMap(m -> {
                    if (m.getX() == move.getX() && m.getY() == move.getY()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                String.format(CELL_X_Y_IS_USED, m.getX(), m.getY())));
                    }
                    return Mono.just(m);
                })
                .collectList()
                .zipWith(gameRepository.findById(gameId)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                String.format(GAME_NOT_FOUND, gameId)))))
                .flatMap(gameWithMoves -> {
                    GameEntity game = gameWithMoves.getT2();
                    List<MoveEntity> moves = gameWithMoves.getT1();
                    int size = gameWithMoves.getT1().size();

                    // If the game is over then stop processing the move
                    if (game.getStatus()
                            .equals(GameStatus.FINISHED)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                GAME_IS_CLOSED));
                    }

                    // Create next move entity, set its properties and add to array of moves
                    MoveEntity moveEntity = moveMapper.dtoToEntity(move);
                    moveEntity.setGameId(gameId);
                    moveEntity.setNumber(size + 1);
                    gameWithMoves.getT1().add(moveEntity);

                    // Process new move differently based on a game type
                    if (game.getGameType().equals(GameType.AGAINST_AI)) {
                        // If this match is against AI then next move has to be processed by AI engine
                        // and automatic answer move has to be produced by machine
                        moveEntity.setPlayedBy(GameWinner.PLAYER);
                        game.setActiveTurn(GameWinner.AI);
                        engineAgainstAi.generateAndProcessNextMove(game, moves);
                    } else {
                        // If this match is against person then simply save next move to DB
                        // and update game properties accordingly
                        if (size == 0) {
                            moveEntity.setPlayedBy(GameWinner.PLAYER_1);
                            game.setActiveTurn(GameWinner.PLAYER_2);
                        } else {
                            String lastPlayedBy = gameWithMoves.getT1().get(size - 1).getPlayedBy();
                            String nextPlayer = lastPlayedBy.equals(GameWinner.PLAYER_1) ?
                                    GameWinner.PLAYER_2 : GameWinner.PLAYER_1;
                            moveEntity.setPlayedBy(nextPlayer);
                            game.setActiveTurn(lastPlayedBy);
                        }
                        engineAgainstPerson.validateNextMove(game, moves);
                    }

                    return Mono.zip(
                            gameRepository.save(game),
                            moveRepository
                                    .saveAll(moves)
                                    .collectList()
                                    .log()
                    );
                })
                .flatMap(gameWithMovesUpdated -> {
                    // Generate DTO to send it as HTTP response
                    GameDto game = gameMapper.dtoToEntity(gameWithMovesUpdated.getT1());
                    game.setMoves(gameWithMovesUpdated
                            .getT2()
                            .stream()
                            .map(moveMapper::entityToDto)
                            .sorted(Comparator.comparing(MoveDto::getNumber))
                            .collect(toCollection(ArrayList::new)));
                    return Mono.just(game);
                })
                .doOnSuccess(e -> log.info("Move was successfully processed"))
                .doOnError(e -> log.info("Failed to add next move"));
    }

    public Mono<GameDto> getGame(String gameId) {
        return gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(GAME_NOT_FOUND, gameId))))
                .map(gameMapper::dtoToEntity)
                .zipWith(moveRepository
                        .findByGameId(gameId)
                        .collectList()
                        .map(m -> m.stream()
                                .map(moveMapper::entityToDto)
                                .sorted(Comparator.comparing(MoveDto::getNumber))
                                .collect(toCollection(ArrayList::new)))
                ).map(gameWithMoves -> {
                    gameWithMoves.getT1().setMoves(gameWithMoves.getT2());
                    return gameWithMoves.getT1();
                })
                .doOnSuccess(e -> log.info("Game was found and fetched"));
    }
}
