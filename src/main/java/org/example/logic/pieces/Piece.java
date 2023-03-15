package org.example.logic.pieces;

import static org.example.util.ByteUtil.getLsb;
import static org.example.util.ByteUtil.getMsb;

public class Piece {
    public static final int NONE = 0;
    public static final int KING = 1;
    public static final int PAWN = 2;
    public static final int KNIGHT = 3;
    public static final int BISHOP = 4;
    public static final int ROOK = 5;
    public static final int QUEEN = 6;

    public static final int WHITE = 16;
    public static final int BLACK = 32;

    public static boolean isColour(int piece, int colour) {
        return getMsb(piece) == colour;
    }

    public static boolean isType(int piece, int type) {
        return getLsb(piece) == type;
    }

    public static String getType(int piece) {
        switch (getLsb(piece)) {
            case (1):
                return "King";
            case (2):
                return "Pawn";
            case (3):
                return "Knight";
            case (4):
                return "Bishop";
            case (5):
                return "Rook";
            case (6):
                return "Queen";
            default:
                return null;
        }
    }

    public static boolean isSlidingPiece(int piece) {
        int pieceType = getLsb(piece);
        return pieceType == 4 || pieceType == 5 || pieceType == 6;
    }

    public static int getOppositeColour(int colour) {
        return colour == WHITE ? BLACK : WHITE;
    }

}
