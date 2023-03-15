package org.example.logic;

import org.example.logic.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.pow;
import static org.example.logic.pieces.Piece.*;
import static org.example.util.PrecomputedMoveData.directionOffsets;
import static org.example.util.PrecomputedMoveData.numSquaresToEdge;

public class MoveGenerator {
    private final long notAFile = 0xFEFEFEFEFEFEFEFEL;
    private final long notABFile = 0xFCFCFCFCFCFCFCFCL;
    private final long notHFile = 0x7F7F7F7F7F7F7F7FL;
    private final long notGHFile = 0x3F3F3F3F3F3F3F3FL;
    private final long rank4 = 0x00000000FF000000L;
    private final long rank5 = 0x000000FF00000000L;
    private long empty;
    private long whitePawns;
    private long blackPawns;
    private long notWhite;
    private long notBlack;
    private long notFriendlyPieces;
    private long taboo;
    private long tabooXRay;
    private List<Move> moves;
    private int friendlyColour;
    private int opponentColour;
    private int[] squares;

    private static long northOne(long b) {
        return b << 8;
    }

    private long northEastOne(long b) {
        return (b << 9) & notAFile;
    }

    private long eastOne(long b) {
        return (b << 1) & notAFile;
    }

    private long southEastOne(long b) {
        return (b >> 7) & notAFile;
    }

    private long southOne(long b) {
        return b >> 8;
    }

    private long southWestOne(long b) {
        return (b >> 9) & notHFile;
    }

    private long westOne(long b) {
        return (b >> 1) & notHFile;
    }

    private long northWestOne(long b) {
        return (b << 7) & notHFile;
    }

    public List<Move> generateMoves(Board board) {

        squares = board.getSquares();

        friendlyColour = board.getColourToMove();
        opponentColour = getOppositeColour(friendlyColour);
        moves = new ArrayList<>();
        generateBitboards();


        int kingPosition = -1;
        boolean friendly;
        for (int i = 0; i < 2; i++) {
            friendly = i != 0;
            for (int startSquare = 0; startSquare < 64; startSquare++) {
                int piece = squares[startSquare];

                if ((friendly && Piece.isColour(piece, opponentColour)) | (!friendly && Piece.isColour(piece, friendlyColour))) {
                    continue;
                }

                if (Piece.isType(piece, BISHOP) || Piece.isType(piece, ROOK) || Piece.isType(piece, QUEEN)) {
                    generateSlidingMoves(startSquare, piece, friendly);
                    continue;
                }
                if (Piece.isType(piece, PAWN)) {
                    generatePawnMoves(startSquare, friendly);
                    continue;
                }
                if (Piece.isType(piece, KNIGHT)) {
                    generateKnightMoves(startSquare, friendly);
                    continue;
                }
                if (Piece.isType(piece, KING)) {
                    kingPosition = startSquare;
                }
            }
            generateKingMoves(kingPosition, friendly);
        }
        checkForChecks(kingPosition);
        return moves;
    }

    private void checkForChecks(int kingPosition) {
        long kingPositionBitboard = addBit(0, kingPosition);
        if ((kingPositionBitboard & taboo) >= 1) {
            System.out.println("CHECK");
        }
    }

    private void generateBitboards() {
        empty = 0;
        whitePawns = 0;
        blackPawns = 0;
        notWhite = 0;
        notBlack = 0;
        taboo = 0;
        tabooXRay = 0;
        generateBitboard(WHITE);
        generateBitboard(BLACK);
        notFriendlyPieces = friendlyColour == WHITE ? notWhite : notBlack;
        empty = notWhite & notBlack;
    }

    private void generateBitboard(int colour) {
        long notBitboard = 0;
        long pawnBitboard = 0;

        for (int i = 0; i < 64; i++) {
            int piece = squares[i];
            int root = i == 63 ? -2 : 2;
            if (Piece.isColour(piece, colour)) {
                if (Piece.isType(piece, PAWN)) {
                    pawnBitboard += (long) pow(root, i);
                }
                continue;
            }
            notBitboard += (long) pow(root, i);
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

    private long addBit(long bitboard, int position) {
        int root = position == 63 ? -2 : 2;
        if (position != 64) {
            return bitboard | (long) pow(root, position);
        }
        return bitboard;
    }

    private void generatePawnMoves(int startSquare, boolean friendly) {
        long binStartSquare = (long) pow(2, startSquare);

        // Taboo logic
        if (!friendly) {
            long attacks = opponentColour == WHITE ? (northEastOne(binStartSquare) | northWestOne(binStartSquare)) : (southEastOne(binStartSquare) | southWestOne(binStartSquare));
            taboo |= attacks;
            return;
        }

        long singleTargets = friendlyColour == WHITE ? northOne(binStartSquare) & empty : southOne(binStartSquare) & empty;
        long doubleTargets = friendlyColour == WHITE ? northOne(singleTargets) & empty & rank4 : southOne(singleTargets) & empty & rank5;
        long attacks = friendlyColour == WHITE ? (northEastOne(binStartSquare) | northWestOne(binStartSquare)) & ~notBlack : (southEastOne(binStartSquare) | southWestOne(binStartSquare)) & ~notWhite;

        long pawnTargets = singleTargets | doubleTargets | attacks;
        addPawnMoves(startSquare, pawnTargets);
    }

    private void addPawnMoves(int startSquare, long pawnTargets) {
        for (int target = 0; target < 64; target++) {
            //If there is a bit switched on
            if (pawnTargets << ~target < 0) {
                addMove(startSquare, target);
            }
        }
    }

    private void generateKnightMoves(int startSquare, boolean friendly) {
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
            if (!friendly) {
                taboo |= position;
                continue;
            }
            long targetSquare = (Long.numberOfTrailingZeros(position & notFriendlyPieces));
            addMove(startSquare, (int) targetSquare);
        }
    }

    private void generateKingMoves(int startSquare, boolean friendly) {
        long binStartSquare = (long) pow(2, startSquare);
        long[] kingTargets = new long[8];

        kingTargets[0] = northWestOne(binStartSquare);
        kingTargets[1] = northOne(binStartSquare);
        kingTargets[2] = northEastOne(binStartSquare);
        kingTargets[3] = southEastOne(binStartSquare);
        kingTargets[4] = southOne(binStartSquare);
        kingTargets[5] = southWestOne(binStartSquare);
        kingTargets[6] = eastOne(binStartSquare);
        kingTargets[7] = westOne(binStartSquare);

        for (long targetSquareBitboard : kingTargets) {
            if (!friendly) {
                taboo |= targetSquareBitboard;
                continue;
            }

            targetSquareBitboard &= ~taboo & notFriendlyPieces;
            int targetSquare = Long.numberOfTrailingZeros(targetSquareBitboard);
            addMove(startSquare, targetSquare);
        }
    }

    private void generateSlidingMoves(int startSquare, int piece, boolean friendly) {
        int startDirIndex = Piece.isType(piece, BISHOP) ? 4 : 0;
        int endDirIndex = Piece.isType(piece, ROOK) ? 4 : 8;
        boolean moveBlocked;
        for (int directionIndex = startDirIndex; directionIndex < endDirIndex; directionIndex++) {
            moveBlocked = false;
            for (int n = 0; n < numSquaresToEdge[startSquare][directionIndex]; n++) {
                int targetSquare = startSquare + directionOffsets[directionIndex] * (n + 1);
                int pieceOnTargetSquare = squares[targetSquare];

                if (!friendly) {
                    if (!moveBlocked) {
                        taboo = addBit(taboo, targetSquare);
                    }
                    if (!moveBlocked && Piece.isColour(pieceOnTargetSquare, opponentColour)) {
                        moveBlocked = true;
                    }
                    tabooXRay = addBit(tabooXRay, targetSquare);

                    continue;
                }

                if (Piece.isColour(pieceOnTargetSquare, friendlyColour)) {
                    moveBlocked = true;
                }
                if (!moveBlocked) {
                    addMove(startSquare, targetSquare);
                }
                if (Piece.isColour(pieceOnTargetSquare, opponentColour)) {
                    moveBlocked = true;
                }
            }
        }
    }

    private void addMove(int start, int target) {
        moves.add(new Move(start, target));
    }

    public long getEmpty() {
        return empty;
    }

    public long getWhitePawns() {
        return whitePawns;
    }

    public long getBlackPawns() {
        return blackPawns;
    }

    public long getNotWhite() {
        return notWhite;
    }

    public long getNotBlack() {
        return notBlack;
    }

    public long getNotFriendlyPieces() {
        return notFriendlyPieces;
    }

    public long getTaboo() {
        return taboo;
    }

    public long getTabooXRay() {
        return tabooXRay;
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
