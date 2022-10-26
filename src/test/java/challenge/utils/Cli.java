package challenge.utils;

import challenge.tictactoe.constant.GameStatus;
import challenge.tictactoe.db.GameEntity;
import challenge.tictactoe.db.MoveEntity;
import challenge.tictactoe.logic.TictactoeGameEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static challenge.utils.BoardUtils.*;

/**
 * Command line interface to play with AI locally
 */
public class Cli {

    public static void main(String[] args) {

        GameEntity game = createNewGame();
        TictactoeGameEngine tictactoeGameEngine = new TictactoeGameEngine();
        Scanner scanner = new Scanner(System.in);
        List<MoveEntity> moves = new ArrayList<>();
        while (!game.getStatus().equals(GameStatus.FINISHED)) {
            System.out.println("Enter x y coordinates");
            String[] coordinates = scanner.nextLine().split("\\s+");
            MoveEntity move = createMove(Integer.parseInt(coordinates[0]),
                    Integer.parseInt(coordinates[1]));
            moves.add(move);
            tictactoeGameEngine.generateAndProcessNextMove(game, moves);
            int[][] matrix = new int[3][3];
            moves.forEach(e -> getMoveOwner(matrix, e));
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    System.out.print("|" + (matrix[i][j] == 0 ? "   " : (matrix[i][j] == 1 ? " X " : " O ")));
                }
                System.out.print("|");
                System.out.println();
            }
            System.out.println("Active turn " + game.getActiveTurn());
            System.out.println("Game " + game.getStatus() + " winner: " + game.getWinner());
        }
        System.out.println("Game " + game.getStatus() + " winner: " + game.getWinner());
    }
}
