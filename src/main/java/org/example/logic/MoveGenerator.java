package main.java.org.example.logic;

import main.java.org.example.logic.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

import static main.java.org.example.logic.Board.squares;
import static main.java.org.example.logic.pieces.Piece.*;
import static main.java.org.example.util.PrecomputedMoveData.directionOffsets;
import static main.java.org.example.util.PrecomputedMoveData.numSquaresToEdge;


public class MoveGenerator {
    private static final int friendlyColour = Board.colourToMove;
    private static final int opponentColour = Piece.getOppositeColour(friendlyColour);
    public static List<Move> moves;
    private static long empty;

    public static List<Move> generateMoves() {

        empty = 0;
        for (int i = 0; i < 64; i++) {
            if (squares[i] > 0) {
                System.out.println(i + " = greater then");
                empty += Math.pow(2, i);
            }
        }
        System.out.println(Long.toBinaryString(empty));

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
        int endDirIndex = Piece.isType(piece, Piece.ROOK) ? 4 : 8;
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

    private static void generatePawnMoves(int startSquare) {}

    private static void generateKnightMoves(int startSquare) {
        long notAFile = 0xFEFEFEFEFEFEFEFEL;
        long notABFile = 0xFCFCFCFCFCFCFCFCL;
        long notHFile = 0x7F7F7F7F7F7F7F7FL;
        long notGHFile = 0x3F3F3F3F3F3F3F3FL;
        long[] possiblePosition = new long[8];

        System.out.println("Position:" + startSquare);

        long binStartSquare = (long) Math.pow(2, startSquare);

        possiblePosition[0] = ((binStartSquare << 17) & notAFile);
        possiblePosition[1] = ((binStartSquare << 10) & notABFile);
        possiblePosition[2] = ((binStartSquare >> 6) & notABFile);
        possiblePosition[3] = ((binStartSquare >> 15) & notAFile);
        possiblePosition[4] = ((binStartSquare << 15) & notHFile);
        possiblePosition[5] = ((binStartSquare << 6) & notGHFile);
        possiblePosition[6] = ((binStartSquare >> 10) & notGHFile);
        possiblePosition[7] = ((binStartSquare >> 17) & notHFile);

        for (long position : possiblePosition) {
            long newPosition = (Long.numberOfTrailingZeros(position));
//            System.out.println(newPosition);
            moves.add(new Move(startSquare, (int) newPosition));
//            System.out.println("\n\nReg old pos: " + startSquare);
//            System.out.println("Old position: " + Long.numberOfTrailingZeros(binStartSquare) + 1);
//            System.out.println("New position: " + newPosition);
        }
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
