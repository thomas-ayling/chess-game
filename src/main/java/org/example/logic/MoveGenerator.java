package org.example.logic;

import org.example.logic.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.pow;
import static org.example.logic.pieces.Piece.*;
import static org.example.util.PrecomputedMoveData.directionOffsets;
import static org.example.util.PrecomputedMoveData.numSquaresToEdge;

public class MoveGenerator {
    protected final long notAFile = 0xFEFEFEFEFEFEFEFEL;
    protected final long notABFile = 0xFCFCFCFCFCFCFCFCL;
    protected final long notHFile = 0x7F7F7F7F7F7F7F7FL;
    protected final long notGHFile = 0x3F3F3F3F3F3F3F3FL;
    protected final long rank4 = 0x00000000FF000000L;
    protected final long rank5 = 0x000000FF00000000L;
    protected long empty;
    protected long whitePawns;
    protected long blackPawns;
    protected long notWhite;
    protected long notBlack;
    protected long notFriendlyPieces;
    protected long taboo;
    protected List<Move> moves;
    protected int friendlyColour;
    protected int opponentColour;
    protected int[] squares;
    protected long opponentMoves = 0;

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
        squares = board.getSquares();

        friendlyColour = opponent ? getOppositeColour(board.getColourToMove()) : board.getColourToMove();
        opponentColour = getOppositeColour(friendlyColour);

        empty = 0;
        whitePawns = 0;
        blackPawns = 0;
        notWhite = 0;
        notBlack = 0;
        taboo = 0;

        generateBitboard(WHITE);
        generateBitboard(BLACK);

        generateTaboo(board, opponent);

        notFriendlyPieces = friendlyColour == WHITE ? notWhite : notBlack;

        empty = notWhite & notBlack;

        moves = new ArrayList<>();

        int kingPosition = -1;

        for (int startSquare = 0; startSquare < 64; startSquare++) {
            int piece = squares[startSquare];
            if (Piece.isColour(piece, friendlyColour)) {
                if (Piece.isType(piece, PAWN)) {
                    generatePawnMoves(startSquare);
                }
                if (Piece.isType(piece, BISHOP) || Piece.isType(piece, ROOK) || Piece.isType(piece, QUEEN)) {
                    generateSlidingMoves(startSquare, piece);
                    continue;
                }
                if (Piece.isType(piece, KNIGHT)) {
                    generateKnightMoves(startSquare);
                }
                if (Piece.isType(piece, KING)) {
                    kingPosition = startSquare;
                }
            }
        }
        generateKingMoves(kingPosition, opponent);
        return moves;
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

    private void generateTaboo(Board board, boolean opponent) {
        if (!opponent) {
            List<Move> opponentMoves = new MoveGenerator().generateMoves(board, true);
            for (Move move : opponentMoves) {
                int root = move.targetSquare == 63 ? -2 : 2;
                if (move.targetSquare != 64) {
                    taboo |= (long) pow(root, move.targetSquare);
                    printBin(taboo);
                }
            }
        }
    }

    protected void generatePawnMoves(int startSquare) {
        //todo: Generate pawn attacks when checking taboo
        long binStartSquare = (long) pow(2, startSquare);
        long singleTargets = friendlyColour == WHITE ? northOne(binStartSquare) & empty : southOne(binStartSquare) & empty;
        long doubleTargets = friendlyColour == WHITE ? northOne(singleTargets) & empty & rank4 : southOne(binStartSquare) & empty & rank5;
        long attacks = friendlyColour == WHITE ? (northEastOne(startSquare) | northWestOne(startSquare)) & ~notBlack : (southEastOne(startSquare) | southWestOne(startSquare)) & ~notWhite;

        long pawnTargets = singleTargets | doubleTargets | attacks;

        for (int i = 0; i < 64; i++) {
            //If there is a bit switched on
            if (pawnTargets << ~i < 0) {
                moves.add(new Move(startSquare, i));
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
            moves.add(new Move(startSquare, (int) targetSquare));
        }
    }

    private void generateKingMoves(int startSquare, boolean opponent) {
        long binStartSquare = (long) pow(2, startSquare);

        long[] kingTargets = new long[8];

        kingTargets[0] = northWestOne(binStartSquare) & notHFile;
        kingTargets[1] = northOne(binStartSquare);
        kingTargets[2] = northEastOne(binStartSquare) & notHFile;
        kingTargets[3] = southEastOne(binStartSquare) & notHFile;
        kingTargets[4] = southOne(binStartSquare);
        kingTargets[5] = southWestOne(binStartSquare) & notAFile;
        kingTargets[6] = eastOne(binStartSquare) & notHFile;
        kingTargets[7] = westOne(binStartSquare) & notHFile;

        for (long target : kingTargets) {
            if (!opponent) {
                target &= ~taboo;
            }
            target &= notBlack;

            target = Long.numberOfTrailingZeros(target);
            moves.add(new Move(startSquare, (int) target));
        }
    }

    private void printBin(long n) {
        System.out.println(String.format("%64s", Long.toBinaryString(n)).replace(' ', '0'));
    }

    private void generateSlidingMoves(int startSquare, int piece) {
        // todo: Fix sliding pieces when calculating taboo squares
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

    public static class Move {
        public final int startSquare;
        public final int targetSquare;

        public Move(int startSquare, int targetSquare) {
            this.startSquare = startSquare;
            this.targetSquare = targetSquare;
        }
    }
}
