package challenge.tictactoe.logic;

import challenge.tictactoe.constant.GameStatus;
import challenge.tictactoe.constant.GameWinner;
import challenge.tictactoe.db.GameEntity;
import challenge.tictactoe.db.MoveEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NOTE: This should be a separate module and packages as Java library, but
 * for the sake of simplicity I will keep this business logic here with the backend.
 * <p>
 * Description of the Minimax algorithm:
 * <p>
 * The essence of the Minimax algorithm is an alternate enumeration of the possible moves
 * of two players, in which we believe that the player "whose turn" will choose the move
 * that brings the maximum number of points. Suppose that we are playing for player "X",
 * then the description of the algorithm will be something like this:
 * <p>
 * If the game is over, return the score for player "X"
 * Otherwise, get a list of new play area states for each possible move
 * Estimate the possible payoff for each possible state
 * For each of the possible states, add a "Minimax" estimate of the current state
 * If player's move is "X", return the move with the maximum score
 * If the player's move is "O", return the move with the minimum number of points
 * <p>
 * The algorithm is recursive using BFS, and the calculation is performed for each of the players in
 * turn until the final payoff is calculated.
 */
public class TictactoeGameEngine {

    private final int PLAYER_1 = 1;
    private final int PLAYER_2 = 2;
    private final int MATRIX_SIZE = 3;
    private final String FIRST_PLAYER;
    private final String SECOND_PLAYER;

    /**
     * Constructor for Human vs AI Tictactoe game
     */
    public TictactoeGameEngine() {
        this.FIRST_PLAYER = GameWinner.PLAYER;
        this.SECOND_PLAYER = GameWinner.AI;
    }

    /**
     * Constructor for Person 1 vs Person 2 Tictactoe game
     */
    public TictactoeGameEngine(boolean twoPlayers) {
        this.FIRST_PLAYER = GameWinner.PLAYER_1;
        this.SECOND_PLAYER = GameWinner.PLAYER_2;
    }

    /**
     * Process new move in Person 1 vs Person 2 game and
     * check if it leads to the end of the game.
     *
     * @param game
     * @param moves
     */
    public void validateNextMove(GameEntity game,
                                 List<MoveEntity> moves) {
        processMove(game, moves, false);
    }

    /**
     * Process new move from Person in the game with AI,
     * generate next move from AI and check if it leads to the end of the game.
     *
     * @param game
     * @param moves
     */
    public void generateAndProcessNextMove(GameEntity game,
                                           List<MoveEntity> moves) {
        processMove(game, moves, true);
    }

    private void processMove(GameEntity game, List<MoveEntity> moves, boolean process) {
        if (game.getStatus().equals(GameStatus.FINISHED))
            return;

        game.setStatus(GameStatus.IN_PROGRESS);
        game.setWinner(GameWinner.NOT_DEFINED_YET);

        if (process) {
            game.setActiveTurn(SECOND_PLAYER);
            miniMax(game.getActiveTurn(), game.getId(), moves, 0);
            game.setActiveTurn(FIRST_PLAYER);
        }

        String winner = getWinner(moves);
        if (!winner.equals(GameWinner.NOT_DEFINED_YET)) {
            game.setStatus(GameStatus.FINISHED);
            game.setWinner(winner);
            game.setActiveTurn(null);
        }
    }

    /**
     * Recursive function for finding the best next move
     *
     * @param playerName
     * @param gameId
     * @param moves
     * @param depth
     * @return
     */
    private Integer miniMax(String playerName,
                            String gameId,
                            List<MoveEntity> moves,
                            int depth) {
        if (!getWinner(moves).equals(GameWinner.NOT_DEFINED_YET)) {
            return getScore(moves, depth);
        }

        ArrayList<Integer> scores = new ArrayList<>();
        ArrayList<MoveEntity> nextMoves = new ArrayList<>();

        int finalDepth = depth + 1;
        getAvailableMoves(playerName, gameId, moves)
                .forEach(move -> {
                    String nextPlayer = getOpponentName(playerName);
                    List<MoveEntity> possibleMoves = createNextMoves(moves, move);
                    scores.add(miniMax(nextPlayer, gameId, possibleMoves, finalDepth));
                    nextMoves.add(move);
                });

        if (scores.size() == 0) return 0;

        if (playerName.equals(SECOND_PLAYER)) {
            int maxScoreIndex = scores.indexOf(Collections.max(scores));
            moves.add(nextMoves.get(maxScoreIndex));
            return scores.get(maxScoreIndex);
        } else {
            int minScoreIndex = scores.indexOf(Collections.min(scores));
            moves.add(nextMoves.get(minScoreIndex));
            return scores.get(minScoreIndex);
        }
    }

    /**
     * Find who turn to make a next move
     */
    private void setMoveOwner(int[][] mat, MoveEntity move) {
        mat[move.getX()][move.getY()] = move.getPlayedBy()
                .equals(FIRST_PLAYER) ? PLAYER_1 : PLAYER_2;
    }

    /**
     * Process Tictactoe board to determine the winner or
     * proceed with the current NOT_DEFINED_YET status.
     * <p>
     * A player wins if they can align 3 of their
     * markers in a vertical, horizontal or diagonal line
     */
    private String getWinner(List<MoveEntity> moves) {
        int[][] tictactoeBoard = new int[MATRIX_SIZE][MATRIX_SIZE];
        moves.forEach(e -> setMoveOwner(tictactoeBoard, e));

        if (checkDiagonal(tictactoeBoard, PLAYER_2)
                || checkAntiDiagonal(tictactoeBoard, PLAYER_2))
            return SECOND_PLAYER;

        for (int i = 0; i < tictactoeBoard.length; i++) {
            if (checkRow(tictactoeBoard, i, PLAYER_2) ||
                    checkCol(tictactoeBoard, i, PLAYER_2)) {
                return SECOND_PLAYER;
            }
        }

        if (checkDiagonal(tictactoeBoard, PLAYER_1)
                || checkAntiDiagonal(tictactoeBoard, PLAYER_1))
            return FIRST_PLAYER;

        for (int i = 0; i < tictactoeBoard.length; i++) {
            if (checkRow(tictactoeBoard, i, PLAYER_1) ||
                    checkCol(tictactoeBoard, i, PLAYER_1)) {
                return FIRST_PLAYER;
            }
        }

        if (moves.size() == MATRIX_SIZE * MATRIX_SIZE)
            return GameWinner.DRAW;

        return GameWinner.NOT_DEFINED_YET;
    }

    /**
     * Find the score of the board based on moves history and depth
     *
     * @param moves
     * @param depth
     * @return
     */
    private int getScore(List<MoveEntity> moves, int depth) {
        if (getWinner(moves).equals(SECOND_PLAYER)) {
            return 10 - depth;
        } else if (getWinner(moves).equals(FIRST_PLAYER)) {
            return depth - 10;
        }
        return 0;
    }

    private String getOpponentName(String player) {
        return player.equals(FIRST_PLAYER) ?
                SECOND_PLAYER :
                FIRST_PLAYER;
    }

    private MoveEntity createNextMove(int x, int y,
                                      List<MoveEntity> moves,
                                      String player,
                                      String gameId) {
        return MoveEntity.builder()
                .x(x)
                .y(y)
                .number(moves.size() + 1)
                .gameId(gameId)
                .playedBy(player)
                .build();
    }

    private List<MoveEntity> getAvailableMoves(String player, String gameId,
                                               List<MoveEntity> moves) {
        int[][] tictactoeBoard = new int[MATRIX_SIZE][MATRIX_SIZE];
        moves.forEach(e -> tictactoeBoard[e.getX()][e.getY()] = 1);
        ArrayList<MoveEntity> newMoves = new ArrayList<>();
        for (int i = 0; i < tictactoeBoard.length; i++) {
            for (int j = 0; j < tictactoeBoard[i].length; j++) {
                if (tictactoeBoard[i][j] == 0) {
                    newMoves.add(createNextMove(i, j, moves, player, gameId));
                }
            }
        }
        return newMoves;
    }

    private List<MoveEntity> createNextMoves(List<MoveEntity> moves, MoveEntity move) {
        List<MoveEntity> movesClone = new ArrayList<>();
        movesClone.addAll(moves);
        movesClone.add(move);
        return movesClone;
    }

    private boolean checkRow(int[][] board, int row, int player) {
        for (int col = 0; col < 3; ++col) {
            if (board[row][col] != player) return false;
        }
        return true;
    }

    private boolean checkCol(int[][] board, int col, int player) {
        for (int row = 0; row < 3; ++row) {
            if (board[row][col] != player) return false;
        }
        return true;
    }

    private boolean checkDiagonal(int[][] board, int player) {
        for (int row = 0; row < 3; ++row) {
            if (board[row][row] != player) return false;

        }
        return true;
    }

    private boolean checkAntiDiagonal(int[][] board, int player) {
        for (int row = 0; row < 3; ++row) {
            if (board[row][2 - row] != player) return false;
        }
        return true;
    }
}
