package org.example.util;

import org.example.logic.pieces.Piece;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.example.logic.pieces.Piece.*;

public class LoadUtil {
    public static String loadFenFromBoard(int[] board) {
        StringBuilder encodedBord = new StringBuilder();
        int i = 0;
        for (int piece : board) {
            i++;
            if (piece == 0) {
                encodedBord.append('1');
            }
            if (piece != 0) {
                switch (getType(piece)) {
                    case "King":
                        encodedBord.append(isColour(piece, BLACK) ? "k" : "K");
                        break;
                    case "Pawn":
                        encodedBord.append(isColour(piece, BLACK) ? "p" : "P");
                        break;
                    case "Knight":
                        encodedBord.append(isColour(piece, BLACK) ? "n" : "N");
                        break;
                    case "Bishop":
                        encodedBord.append(isColour(piece, BLACK) ? "b" : "B");
                        break;
                    case "Rook":
                        encodedBord.append(isColour(piece, BLACK) ? "r" : "R");
                        break;
                    case "Queen":
                        encodedBord.append(isColour(piece, BLACK) ? "q" : "Q");
                        break;
                }
            }
            if (i % 8 == 0) {
                encodedBord.append('/');
            }
        }
        return encodedBord.toString();
    }

    public static int[] loadBoardFromFen(String fen) {
        Map<Character, Integer> pieceTypeFromSymbol = new HashMap<>() {{
            put('k', Piece.KING);
            put('p', Piece.PAWN);
            put('n', Piece.KNIGHT);
            put('b', Piece.BISHOP);
            put('r', Piece.ROOK);
            put('q', Piece.QUEEN);
        }};

        int file = 0, rank = 7;
        int[] squares = new int[64];

        String fenBoard = fen.split(" ")[0];

        for (char symbol : fenBoard.toCharArray()) {
            if (symbol == '/') {
                file = 0;
                rank--;
                continue;
            }
            if (Character.isDigit(symbol)) {
                file += Character.getNumericValue(symbol);
                continue;
            }
            int colour = (Character.isUpperCase(symbol) ? Piece.WHITE : BLACK);
            int type = pieceTypeFromSymbol.get(Character.toLowerCase(symbol));
            squares[file + (rank * 8)] = type | colour;
            file++;
        }
        return squares;
    }

    public static int[] loadSquaresFromFile() {
        int[] squares = new int[64];
        try {
            File fenFile = new File("src/main/resources/initial-board.fen");
            Scanner scanner = new Scanner(fenFile);
            String fenString = scanner.next();
            squares = LoadUtil.loadBoardFromFen(fenString);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
        return squares;
    }
}
