package challenge.tictactoe.logic;

import challenge.tictactoe.constant.GameStatus;
import challenge.tictactoe.constant.GameWinner;
import challenge.tictactoe.db.GameEntity;
import challenge.tictactoe.db.MoveEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static challenge.utils.BoardUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("component-test")
@Slf4j
class TictactoeGameEngineTest {

    TictactoeGameEngine tictactoeGameEngine = new TictactoeGameEngine();

    /**
     * | O |   |   |
     * |   | X |   |
     * |   |   |   |
     */
    @Test
    void testFirstCornerMoveAiShouldTakeCenter() {
        GameEntity game = createNewGame();
        List<MoveEntity> moves = new ArrayList<>();
        MoveEntity move = createMove(0, 0);
        moves.add(move);
        makeIntermediateMoveAndAssert(moves, game, tictactoeGameEngine, 0, 0);
        MoveEntity lastMove = moves.get(moves.size() - 1);
        assertEquals(lastMove.getX(), 1);
        assertEquals(lastMove.getY(), 1);
    }

    /**
     * | O | X | O |
     * | O | X | X |
     * | X | O | X |
     */
    @Test
    void testDrawMatchWhenUserStarts() {
        GameEntity game = createNewGame();
        List<MoveEntity> moves = new ArrayList<>();
        makeIntermediateMoveAndAssert(moves, game, tictactoeGameEngine, 1, 1);
        makeIntermediateMoveAndAssert(moves, game, tictactoeGameEngine, 2, 0);
        makeIntermediateMoveAndAssert(moves, game, tictactoeGameEngine, 0, 1);
        makeIntermediateMoveAndAssert(moves, game, tictactoeGameEngine, 2, 2);
        makeFinalMoveAndAssert(moves, game, tictactoeGameEngine, 1, 2);
    }

    /**
     * | O | O | X |
     * | X | X | O |
     * | O | X | O |
     * Game FINISHED winner: DRAW
     */
    @Test
    void testDrawMatchWhenAiStartsFirst() {
        GameEntity game = createNewGame();
        List<MoveEntity> moves = new ArrayList<>();
        tictactoeGameEngine.generateAndProcessNextMove(game, moves);
        Assertions.assertEquals(game.getStatus(), GameStatus.IN_PROGRESS);
        Assertions.assertEquals(game.getWinner(), GameWinner.NOT_DEFINED_YET);
        makeIntermediateMoveAndAssert(moves, game, tictactoeGameEngine, 1, 1);
        makeIntermediateMoveAndAssert(moves, game, tictactoeGameEngine, 0, 2);
        makeIntermediateMoveAndAssert(moves, game, tictactoeGameEngine, 1, 0);
        makeFinalMoveAndAssert(moves, game, tictactoeGameEngine, 2, 1);
    }

    /**
     * | X |   | X |
     * | O | O | O |
     * |   |   | X |
     */
    @Test
    void testLostMatchInThreeMovesWhenUserStartsFirst() {
        GameEntity game = createNewGame();
        List<MoveEntity> moves = new ArrayList<>();
        makeIntermediateMoveAndAssert(moves, game, tictactoeGameEngine, 2, 2);
        makeIntermediateMoveAndAssert(moves, game, tictactoeGameEngine, 0, 2);
        moves.add(createMove(0, 0));
        tictactoeGameEngine.generateAndProcessNextMove(game, moves);
        assertEquals(game.getStatus(), GameStatus.FINISHED);
        assertEquals(game.getWinner(), GameWinner.AI);
    }

    /**
     * | O | O | O |
     * |   |   | X |
     * |   |   | X |
     * Winner: AI
     */
    @Test
    void testLostMatchInThreeMovesWhenAiStartsFirst() {
        GameEntity game = createNewGame();
        List<MoveEntity> moves = new ArrayList<>();
        tictactoeGameEngine.generateAndProcessNextMove(game, moves);
        assertEquals(game.getStatus(), GameStatus.IN_PROGRESS);
        assertEquals(game.getWinner(), GameWinner.NOT_DEFINED_YET);
        makeIntermediateMoveAndAssert(moves, game, tictactoeGameEngine, 2, 2);
        moves.add(createMove(1, 2));
        tictactoeGameEngine.generateAndProcessNextMove(game, moves);
        assertEquals(game.getStatus(), GameStatus.FINISHED);
        assertEquals(game.getWinner(), GameWinner.AI);
    }

    @Test
    void testAiVsAi() {
        GameEntity gameAi1 = createNewGame();
        GameEntity gameAi2 = createNewGame();
        List<MoveEntity> movesAi1 = new ArrayList<>();
        List<MoveEntity> movesAi2 = new ArrayList<>();
        tictactoeGameEngine.generateAndProcessNextMove(gameAi1, movesAi1);
        assertEquals(gameAi1.getStatus(), GameStatus.IN_PROGRESS);
        assertEquals(gameAi1.getWinner(), GameWinner.NOT_DEFINED_YET);
        while (!gameAi2.getStatus().equals(GameStatus.FINISHED)) {
            int xAi2 = movesAi1.get(movesAi1.size() - 1).getX();
            int yAi2 = movesAi1.get(movesAi1.size() - 1).getY();
            makeMoveWithoutAssertion(movesAi2, gameAi2, tictactoeGameEngine, xAi2, yAi2);
            int xAi1 = movesAi2.get(movesAi2.size() - 1).getX();
            int yAi1 = movesAi2.get(movesAi2.size() - 1).getY();
            makeMoveWithoutAssertion(movesAi1, gameAi1, tictactoeGameEngine, xAi1, yAi1);
        }
        assertEquals(gameAi1.getStatus(), GameStatus.FINISHED);
        assertEquals(gameAi1.getWinner(), GameWinner.DRAW);
        assertNull(gameAi1.getActiveTurn());
        assertEquals(gameAi2.getStatus(), GameStatus.FINISHED);
        assertEquals(gameAi2.getWinner(), GameWinner.DRAW);
        assertNull(gameAi2.getActiveTurn());
    }

    @Test
    void testAiPerformance() {
        int RUNS_COUNT = 100;
        List<List<Long>> timeProbes = new ArrayList<>();
        for (int i = 0; i < RUNS_COUNT; i++) {
            int level = 0;
            GameEntity gameAi1 = createNewGame();
            GameEntity gameAi2 = createNewGame();
            List<MoveEntity> movesAi1 = new ArrayList<>();
            List<MoveEntity> movesAi2 = new ArrayList<>();

            long start = System.nanoTime();
            tictactoeGameEngine.generateAndProcessNextMove(gameAi1, movesAi1);
            long finish = System.nanoTime();
            addValueToList(finish - start, level, timeProbes);
            while (!gameAi2.getStatus().equals(GameStatus.FINISHED)) {
                level++;
                int xAi2 = movesAi1.get(movesAi1.size() - 1).getX();
                int yAi2 = movesAi1.get(movesAi1.size() - 1).getY();
                long time = makeMoveWithoutAssertion(movesAi2, gameAi2, tictactoeGameEngine, xAi2, yAi2);
                addValueToList(time, level, timeProbes);
                if (!gameAi1.getStatus().equals(GameStatus.FINISHED)) {
                    level++;
                    int xAi1 = movesAi2.get(movesAi2.size() - 1).getX();
                    int yAi1 = movesAi2.get(movesAi2.size() - 1).getY();
                    time = makeMoveWithoutAssertion(movesAi1, gameAi1, tictactoeGameEngine, xAi1, yAi1);
                    addValueToList(time, level, timeProbes);
                }
            }
        }
        List<Double> averageTime = timeProbes
                .stream()
                .map(e ->
                        e.stream()
                                .mapToDouble(a -> (double) a / 1_000_000_000)
                                .average()
                                .orElse(Double.NaN)
                )
                .collect(Collectors.toList());

        // Assert that each next level takes less time to process than previous one
        for (int i = 0; i < averageTime.size() - 1; i++) {
            assertTrue(averageTime.get(i) > averageTime.get(i + 1), "Wrong time " + i);
        }

        log.info("AI processing time performance statistics on each level of game:");
        for (int i = 0; i < averageTime.size(); i++) {
            log.info("Level {}: {} seconds", i, averageTime.get(i));
        }
    }
}