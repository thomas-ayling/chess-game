package main.java.org.example.logic;

import main.java.org.example.logic.pieces.Piece;
import main.java.org.example.util.LoadUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class Board {
    public static int[] squares;
    public static Object ColourToMove;
    public static int colourToMove = Piece.WHITE;

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
    }

    public static void move() {

    }

}