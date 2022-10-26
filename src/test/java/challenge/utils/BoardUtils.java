package challenge.utils;

import challenge.tictactoe.constant.GameStatus;
import challenge.tictactoe.constant.GameWinner;
import challenge.tictactoe.db.GameEntity;
import challenge.tictactoe.db.MoveEntity;
import challenge.tictactoe.logic.TictactoeGameEngine;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BoardUtils {

    public static int getMoveOwner(int[][] mat, MoveEntity move) {
        return mat[move.getX()][move.getY()] = move
                .getPlayedBy()
                .equals(GameWinner.PLAYER) ? 1 : 2;
    }

    public static GameEntity createNewGame() {
        return GameEntity.builder()
                .status(GameStatus.CREATED)
                .activeTurn(GameWinner.AI)
                .winner(GameWinner.NOT_DEFINED_YET)
                .id("1")
                .build();
    }

    public static MoveEntity createMove(int x, int y) {
        return MoveEntity.builder()
                .playedBy(GameWinner.PLAYER)
                .x(x)
                .y(y)
                .number(1)
                .gameId("1")
                .build();
    }

    public static long makeMoveWithoutAssertion(List<MoveEntity> moves,
                                                GameEntity game,
                                                TictactoeGameEngine engine,
                                                int x, int y) {
        moves.add(createMove(x, y));
        long start = System.nanoTime();
        engine.processAndGenerareteNextMove(game, moves);
        long finish = System.nanoTime();
        return finish - start;
    }

    public static void makeIntermediateMoveAndAssert(List<MoveEntity> moves,
                                                     GameEntity game,
                                                     TictactoeGameEngine engine,
                                                     int x, int y) {
        moves.add(createMove(x, y));
        engine.processAndGenerareteNextMove(game, moves);
        assertEquals(game.getStatus(), GameStatus.IN_PROGRESS);
        assertEquals(game.getWinner(), GameWinner.NOT_DEFINED_YET);
        assertEquals(game.getActiveTurn(), GameWinner.PLAYER);
    }

    public static void makeFinalMoveAndAssert(List<MoveEntity> moves,
                                              GameEntity game,
                                              TictactoeGameEngine engine,
                                              int x, int y) {
        moves.add(createMove(x, y));
        engine.processAndGenerareteNextMove(game, moves);
        assertEquals(game.getStatus(), GameStatus.FINISHED);
        assertEquals(game.getWinner(), GameWinner.DRAW);
        assertNull(game.getActiveTurn());
    }

    public static void addValueToList(long value, int index, List<List<Long>> list) {
        if (list.size() <= index) {
            list.add(new ArrayList<>());
        }

        list.get(index).add(value);
    }
}
