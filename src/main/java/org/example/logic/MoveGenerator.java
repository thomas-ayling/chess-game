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
    private List<Move> opponentMoves;
    private int friendlyColour;
    private int opponentColour;
    private int[] squares;
    private boolean opponent;

    protected static long northOne(long b) {
        return b << 8;
    }

    protected long northEastOne(long b) {
        return (b << 9) & notAFile;
    }

    protected long eastOne(long b) {
        return (b << 1) & notAFile;
    }

    protected long southEastOne(long b) {
        return (b >> 7) & notAFile;
    }

    protected long southOne(long b) {
        return b >> 8;
    }

    protected long southWestOne(long b) {
        return (b >> 9) & notHFile;
    }

    protected long westOne(long b) {
        return (b >> 1) & notHFile;
    }

    protected long northWestOne(long b) {
        return (b << 7) & notHFile;
    }

    public List<Move> generateMoves(Board board, boolean opponent) {
        this.opponent = opponent;

        squares = board.getSquares();

        friendlyColour = opponent ? getOppositeColour(board.getColourToMove()) : board.getColourToMove();
        opponentColour = getOppositeColour(friendlyColour);
        moves = new ArrayList<>();
        opponentMoves = opponent ? new ArrayList<>() : new MoveGenerator().generateMoves(board, true);
        generateBitboards();


        int kingPosition = -1;

        for (int startSquare = 0; startSquare < 64; startSquare++) {
            int piece = squares[startSquare];
            if (Piece.isType(piece, BISHOP) || Piece.isType(piece, ROOK) || Piece.isType(piece, QUEEN)) {
                generateSlidingMoves(startSquare, piece);
                continue;
            }
            if (Piece.isColour(piece, friendlyColour)) {
                if (Piece.isType(piece, PAWN)) {
                    generatePawnMoves(startSquare);
                }
                if (Piece.isType(piece, KNIGHT)) {
                    generateKnightMoves(startSquare);
                }
                if (Piece.isType(piece, KING)) {
                    kingPosition = startSquare;
                }
            }
        }
        generateKingMoves(kingPosition);
        checkForChecks(kingPosition);
        return moves;
    }

    private void checkForChecks(int friendlyKingPosition) {
        for (Move move : opponentMoves) {
            System.out.printf("%s, %s\n", move.startSquare, move.targetSquare);
            if (move.targetSquare == friendlyKingPosition) {
                System.out.println("CHECK");
            }
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
        if (!opponent) {
            generateTaboo();
        }
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

    private void generateTaboo() {
        for (Move move : opponentMoves) {
            taboo = addBit(taboo, move.targetSquare);
        }
    }

    private long addBit(long bitboard, int position) {
        int root = position == 63 ? -2 : 2;
        if (position != 64) {
            return bitboard | (long) pow(root, position);
        }
        return bitboard;
    }

    protected void generatePawnMoves(int startSquare) {
        long binStartSquare = (long) pow(2, startSquare);

        // Taboo logic
        if (opponent) {
            long attacks = friendlyColour == WHITE ? (northEastOne(binStartSquare) | northWestOne(binStartSquare)) : (southEastOne(binStartSquare) | southWestOne(binStartSquare));
            addPawnMoves(startSquare, attacks);
            return;
        }

        long singleTargets = friendlyColour == WHITE ? northOne(binStartSquare) & empty : southOne(binStartSquare) & empty;
        long doubleTargets = friendlyColour == WHITE ? northOne(singleTargets) & empty & rank4 : southOne(singleTargets) & empty & rank5;
        long attacks = friendlyColour == WHITE ? (northEastOne(binStartSquare) | northWestOne(binStartSquare)) & ~notBlack : (southEastOne(binStartSquare) | southWestOne(binStartSquare)) & ~notWhite;

        long pawnTargets = singleTargets | doubleTargets | attacks;
        addPawnMoves(startSquare, pawnTargets);
    }


    private void addPawnMoves(int startSquare, long pawnTargets) {
        for (int i = 0; i < 64; i++) {
            //If there is a bit switched on
            if (pawnTargets << ~i < 0) {
                addMove(startSquare, i);
            }
        }
    }


    private void generateKnightMoves(int startSquare) {
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
            long targetSquare = (Long.numberOfTrailingZeros(position & notFriendlyPieces));
            addMove(startSquare, (int) targetSquare);
        }
    }

    private void generateKingMoves(int startSquare) {
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

        for (long targetSquare : kingTargets) {
            if (!opponent) {
                targetSquare &= ~taboo & notFriendlyPieces;
            }
            targetSquare = Long.numberOfTrailingZeros(targetSquare);
            addMove(startSquare, (int) targetSquare);
        }
    }

    private void addMove(int start, int target) {
        moves.add(new Move(start, target));
    }

    private void generateSlidingMoves(int startSquare, int piece) {
        int startDirIndex = Piece.isType(piece, BISHOP) ? 4 : 0;
        int endDirIndex = Piece.isType(piece, ROOK) ? 4 : 8;
        boolean moveBlocked;
        for (int directionIndex = startDirIndex; directionIndex < endDirIndex; directionIndex++) {
            moveBlocked = false;
            for (int n = 0; n < numSquaresToEdge[startSquare][directionIndex]; n++) {
                int targetSquare = startSquare + directionOffsets[directionIndex] * (n + 1);
                int pieceOnTargetSquare = squares[targetSquare];
                if (Piece.isColour(pieceOnTargetSquare, friendlyColour)) {
                    if (opponent && !moveBlocked) {
                        addMove(startSquare, targetSquare);
                    }
                    moveBlocked = true;
                }
                if (!moveBlocked) {
                    addMove(startSquare, targetSquare);
                }
                if (Piece.isColour(pieceOnTargetSquare, opponentColour)) {
                    moveBlocked = true;
                }
                if (Piece.isColour(squares[startSquare], opponentColour) && !opponent) {
                    tabooXRay = addBit(tabooXRay, targetSquare);
                }
            }
        }
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
