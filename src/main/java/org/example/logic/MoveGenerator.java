package org.example.logic;

import main.java.org.example.logic.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.pow;
import static main.java.org.example.logic.Board.colourToMove;
import static main.java.org.example.logic.Board.squares;
import static main.java.org.example.logic.pieces.Piece.*;
import static main.java.org.example.util.PrecomputedMoveData.directionOffsets;
import static main.java.org.example.util.PrecomputedMoveData.numSquaresToEdge;


public class MoveGenerator {
    private static final int friendlyColour = colourToMove;
    private static final int opponentColour = Piece.getOppositeColour(friendlyColour);
    public static List<Move> moves;
    public static long empty;
    public static long whitePawns;
    public static long blackPawns;
    public static long notWhite;
    public static long notBlack;


    static long notAFile = 0xFEFEFEFEFEFEFEFEL;
    static long notABFile = 0xFCFCFCFCFCFCFCFCL;
    static long notHFile = 0x7F7F7F7F7F7F7F7FL;
    static long notGHFile = 0x3F3F3F3F3F3F3F3FL;

    public static void generateBitboard(int colour) {
        long notBitboard = 0;
        long pawnBitboard = 0;

        for (int i = 0; i < 64; i++) {
            int piece = squares[i];
            if (!Piece.isColour(piece, colour)) {
                int root = i == 63 ? -2 : 2;
                if (Piece.isType(piece, PAWN)) {
                    pawnBitboard += (long) pow(root, i);
                }
                notBitboard += (long) pow(root, i);
            }
        }

        if (Piece.isColour(colour, WHITE)) {
            notWhite = notBitboard;
            whitePawns = pawnBitboard;
            return;
        }

        if (Piece.isColour(colour, BLACK)) {
            notBlack = notBitboard;
            blackPawns = pawnBitboard;
        }
    }

    public static List<Move> generateMoves() {
        empty = 0;
        whitePawns = 0;
        blackPawns = 0;
        notWhite = 0;
        notBlack = 0;

        generateBitboard(WHITE);
        generateBitboard(BLACK);

        empty = notWhite & notBlack;


        moves = new ArrayList<>();
        for (int startSquare = 0; startSquare < 64; startSquare++) {
            int piece = squares[startSquare];
            if (Piece.isColour(piece, friendlyColour)) {
                if (Piece.isType(piece, BISHOP) || Piece.isType(piece, ROOK) || Piece.isType(piece, QUEEN)) {
                    generateSlidingMoves(startSquare, piece);
                    continue;
                }
                if (Piece.isType(piece, KNIGHT)) {
                    generateKnightMoves(startSquare);
                }
            }
        }
        return moves;
    }

    private static void generateSlidingMoves(int startSquare, int piece) {
        int startDirIndex = Piece.isType(piece, BISHOP) ? 4 : 0;
        int endDirIndex = Piece.isType(piece, ROOK) ? 4 : 8;
        for (int directionIndex = startDirIndex; directionIndex < endDirIndex; directionIndex++) {
            for (int n = 0; n < numSquaresToEdge[startSquare][directionIndex]; n++) {
                int targetSquare = startSquare + directionOffsets[directionIndex] * (n + 1);
                int pieceOnTargetSquare = squares[targetSquare];
                if (Piece.isColour(pieceOnTargetSquare, friendlyColour)) {
                    break;
                }
                moves.add(new Move(startSquare, targetSquare));
                if (Piece.isColour(pieceOnTargetSquare, opponentColour)) {
                    if (Piece.isSlidingPiece(piece)) {
                        break;
                    }
                }
            }
        }
    }

    private static void generatePawnMoves(int startSquare) {
    }

    private static void generateKnightMoves(int startSquare) {
        long[] possiblePosition = new long[8];

        long binStartSquare = (long) pow(2, startSquare);

        possiblePosition[0] = ((binStartSquare << 17) & notAFile);
        possiblePosition[1] = ((binStartSquare << 10) & notABFile);
        possiblePosition[2] = ((binStartSquare >> 6) & notABFile);
        possiblePosition[3] = ((binStartSquare >> 15) & notAFile);
        possiblePosition[4] = ((binStartSquare << 15) & notHFile);
        possiblePosition[5] = ((binStartSquare << 6) & notGHFile);
        possiblePosition[6] = ((binStartSquare >> 10) & notGHFile);
        possiblePosition[7] = ((binStartSquare >> 17) & notHFile);

        for (long position : possiblePosition) {
            long targetPosition = (Long.numberOfTrailingZeros(position));
            moves.add(new Move(startSquare, (int) targetPosition));
        }
    }

    private long southOne(long b) {
        return b >> 8;
    }

    private long northOne(long b) {
        return b << 8;
    }

    private void singlePushPawns() {
    }

    public static class Move {
        public final int startSquare;
        public final int targetSquare;

        public Move(int startSquare, int targetSquare) {
            this.startSquare = startSquare;
            this.targetSquare = targetSquare;
        }
    }

}
