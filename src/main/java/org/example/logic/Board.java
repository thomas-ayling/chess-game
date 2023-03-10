package org.example.logic;

import org.example.util.LoadUtil;
import org.example.logic.pieces.Piece;
import org.example.logic.MoveGenerator.Move;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Board {
    private int[] squares;
    private int colourToMove = Piece.BLACK;
    private List<Move> moves;
    private final MoveGenerator moveGenerator = new MoveGenerator();

    public Board() {
        try {
//            File fenFile = new File("src/main/resources/initial-board.fen");
            File fenFile = new File("src/main/resources/initial-board.fen");
            Scanner scanner = new Scanner(fenFile);
            String fenString = scanner.next();
            squares = LoadUtil.LoadBoardFromFen(fenString);
            System.out.println(fenString);
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.out.println(Arrays.toString(squares));
        moves = moveGenerator.generateMoves(this, false);
    }

    public static void move() {

    }

    public int[] getSquares() {
        return squares;
    }

    public int getColourToMove() {
        return colourToMove;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public long getTaboo() {
        return moveGenerator.getTaboo();
    }
}