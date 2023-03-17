package org.example.util;

import org.example.logic.pieces.Piece;

import java.util.HashMap;
import java.util.Map;

public class LoadUtil {
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
            int colour = (Character.isUpperCase(symbol) ? Piece.WHITE : Piece.BLACK);
            int type = pieceTypeFromSymbol.get(Character.toLowerCase(symbol));
            squares[file + (rank * 8)] = type | colour;
            file++;
        }
        return squares;
    }
}
