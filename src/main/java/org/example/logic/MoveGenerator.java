package org.example.logic;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;
import static org.example.logic.pieces.Piece.*;
import static org.example.util.ByteUtil.printBin;
import static org.example.util.PrecomputedMoveData.directionOffsets;
import static org.example.util.PrecomputedMoveData.numSquaresToEdge;


public class MoveGenerator {
    private final long notAFile = 0xFEFEFEFEFEFEFEFEL;
    private final long notHFile = 0x7F7F7F7F7F7F7F7FL;
    private long pinnedPieces = 0;
    private long empty;
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
        resetVariables(board);
        generateBitboards();

        int kingPosition = -1;
        boolean friendly;
        for (int i = 0; i < 2; i++) {
            friendly = i != 0;
            for (int startSquare = 0; startSquare < 64; startSquare++) {
                int piece = squares[startSquare];
                if ((friendly && isColour(piece, opponentColour)) | (!friendly && isColour(piece, friendlyColour))) {
                    continue;
                }

                if (isType(piece, BISHOP) || isType(piece, ROOK) || isType(piece, QUEEN)) {
                    generateSlidingMoves(startSquare, piece, friendly);
                    continue;
                }
                if (isType(piece, PAWN)) {
                    generatePawnMoves(startSquare, friendly);
                    continue;
                }
                if (isType(piece, KNIGHT)) {
                    generateKnightMoves(startSquare, friendly);
                    continue;
                }
                if (isType(piece, KING)) {
                    kingPosition = startSquare;
                }
            }
            generateKingMoves(kingPosition, friendly);
        }
        checkKingLegality(kingPosition);
        generateLegalMoves(kingPosition);
        return moves;
    }

    private void removeMoves(int position) {
        List<Move> movesToRemove = new ArrayList<>();

        for (Move move : moves) {
            if (move.startSquare == position) {
                movesToRemove.add(move);
            }
        }
        moves.removeAll(movesToRemove);
    }

    private void generateLegalMoves(int kingPosition) {
        List<Integer> pinnedPiecePositions = getPositionsFromBitboard(pinnedPieces);


        for (int position : pinnedPiecePositions) {
            int pinnedPiece = squares[position];


            // If a knight is pinned it cannot move, so we move on to the next pinned piece if any
            if (isType(pinnedPiece, KNIGHT)) {
                continue;
            }

            int xDist = (position % 8) - (kingPosition % 8);
            int yDist = (int) ((int) floor(position / 8f) - floor(kingPosition / 8f));

            int directionToKing = getDirectionToKing(xDist, yDist);
            int directionToPinningPiece = getOppositeDirection(directionToKing);

            int distanceToKing = (int) sqrt(pow(xDist, 2) + pow(yDist, 2));

            if (isType(pinnedPiece, PAWN)) {
                List<Move> movesToRemove = new ArrayList<>();
                for (Move move : moves) {
                    // if (positions is not start of a possible move) or (move attacks the pinning piece) then check next move, otherwise remove the move
                    if (move.startSquare != position || directionOffsets[directionToPinningPiece] == move.targetSquare - move.startSquare) {
                        continue;
                    }
                    movesToRemove.add(move);
                }
                moves.removeAll(movesToRemove);
                continue;
            }

            removeMoves(position);

            // Rooks cannot move diagonally so if the direction index is greater than 3 (meaning a diagonal move) we move on to the next pinned piece if any
            if (isType(pinnedPiece, ROOK) && directionToKing > 3) {
                continue;
            }
            // Bishops cannot move orthogonally so if the direction index is less than 4 (meaning an orthogonal move) we move on to the next pinned piece if any
            if (isType(pinnedPiece, BISHOP) && directionToKing < 4) {
                continue;
            }

            // First, calculate moves to king
            for (int n = 0; n < distanceToKing; n++) {
                int targetSquare = position + directionOffsets[directionToKing] * (n + 1);
                int pieceOnTargetSquare = squares[targetSquare];
                if (pieceOnTargetSquare == 0) {
                    addMove(position, targetSquare);
                    continue;
                }
                break;
            }
            // Then, calculate moves to pinning piece
            for (int n = 0; n < numSquaresToEdge[position][directionToPinningPiece]; n++) {
                int targetSquare = position + directionOffsets[directionToPinningPiece] * (n + 1);
                int pieceOnTargetSquare = squares[targetSquare];
                addMove(position, targetSquare);
                if (pieceOnTargetSquare > 0) {
                    break;
                }
            }
        }
    }

    /**
     * @param xDist the x distance between pinned piece and king.
     * @param yDist the y distance between pinned piece and king.
     * @return an index referring to the direction offsets as defined in PrecomputedMoveData.
     * @see org.example.util.PrecomputedMoveData
     */
    private int getDirectionToKing(int xDist, int yDist) {
        if (xDist == 0 && yDist > 0) {
            return 0; // North
        }
        if (xDist == 0 && yDist < 0) {
            return 1; // South
        }
        if (xDist > 0 && yDist == 0) {
            return 2; // East
        }
        if (xDist < 0 && yDist == 0) {
            return 3; // West
        }
        if (xDist > 0 && yDist < 0) {
            return 4; // South East
        }
        if (xDist < 0 && yDist > 0) {
            return 5; // North West
        }
        if (xDist < 0 && yDist < 0) {
            return 6; // South West
        }
        if (xDist > 0 && yDist > 0) {
            return 7; // North East
        }
        // TODO: Change for logger
        System.out.println("There was an error in direction to king method");
        return -1;
    }

    private int getOppositeDirection(int direction) {
        switch (direction) {
            case 0:
                return 1;
            case 1:
                return 0;
            case 2:
                return 3;
            case 3:
                return 2;
            case 4:
                return 5;
            case 5:
                return 4;
            case 6:
                return 7;
            case 7:
                return 6;
            default:
                // TODO: Change for logger
                System.out.println("There was an error in opposite direction method");
                return -1;
        }
    }

    private void resetVariables(Board board) {
        squares = board.getSquares();
        friendlyColour = board.getColourToMove();
        opponentColour = getOppositeColour(friendlyColour);
        moves = new ArrayList<>();
        // Reset bitboards
        empty = 0;
        notWhite = 0;
        notBlack = 0;
        taboo = 0;
        tabooXRay = 0;
        pinnedPieces = 0;
    }

    private void generateBitboards() {
        generateBitboard(WHITE);
        generateBitboard(BLACK);
        notFriendlyPieces = friendlyColour == WHITE ? notWhite : notBlack;
        empty = notWhite & notBlack;
    }

    private void generateBitboard(int colour) {
        long notBitboard = 0;

        for (int i = 0; i < 64; i++) {
            int piece = squares[i];
            int root = i == 63 ? -2 : 2;
            if (isColour(piece, colour)) {
                continue;
            }
            notBitboard += (long) pow(root, i);
        }

        if (isColour(colour, WHITE)) {
            notWhite = notBitboard;
            return;
        }

        if (isColour(colour, BLACK)) {
            notBlack = notBitboard;
        }
    }

    private void checkKingLegality(int kingPosition) {
        long kingPositionBitboard = addBit(0, kingPosition);
        if (!checkForChecks(kingPositionBitboard)) {
            checkForPinnedPieces(kingPosition, kingPositionBitboard);
        }
        printBin(pinnedPieces);
    }

    private boolean checkForChecks(long kingPositionBitboard) {
        if ((kingPositionBitboard & taboo) >= 1) {
            System.out.println("CHECK");
            return true;
        }
        return false;
    }

    private void checkForPinnedPieces(int kingPosition, long kingPositionBitboard) {
        if ((kingPositionBitboard & tabooXRay) == 0) {
            return;
        }

        // Sliding logic stemming from king
        for (int directionIndex = 0; directionIndex < 8; directionIndex++) {
            List<Integer> potentialPinnedPiecePositions = new ArrayList<>();
            // For each square stemming from the king position in certain direction
            for (int n = 0; n < numSquaresToEdge[kingPosition][directionIndex]; n++) {
                int targetSquare = kingPosition + directionOffsets[directionIndex] * (n + 1);
                int pieceOnTargetSquare = squares[targetSquare];

                if (pieceOnTargetSquare == 0) {
                    continue;
                }
                if (potentialPinnedPiecePositions.size() > 1) {
                    break;
                }
                if (isSlidingPiece(pieceOnTargetSquare) && isColour(pieceOnTargetSquare, opponentColour)) {
                    if (potentialPinnedPiecePositions.size() > 0) {
                        pinnedPieces = addBit(pinnedPieces, potentialPinnedPiecePositions.get(0));
                        break;
                    }
                    continue;
                }
                potentialPinnedPiecePositions.add(targetSquare);
            }
        }
    }

    private long generatePawnAttacks(long binStartSquare) {
        return friendlyColour == WHITE ? (northEastOne(binStartSquare) | northWestOne(binStartSquare)) & ~notBlack : (southEastOne(binStartSquare) | southWestOne(binStartSquare)) & ~notWhite;
    }

    private void generatePawnMoves(int startSquare, boolean friendly) {
        long rank4 = 0x00000000FF000000L;
        long rank5 = 0x000000FF00000000L;

        long binStartSquare = (long) pow(2, startSquare);

        // Taboo logic
        if (!friendly) {
            long attacks = opponentColour == WHITE ? (northEastOne(binStartSquare) | northWestOne(binStartSquare)) : (southEastOne(binStartSquare) | southWestOne(binStartSquare));
            taboo |= attacks;
            return;
        }

        long singleTargets = friendlyColour == WHITE ? northOne(binStartSquare) & empty : southOne(binStartSquare) & empty;
        long doubleTargets = friendlyColour == WHITE ? northOne(singleTargets) & empty & rank4 : southOne(singleTargets) & empty & rank5;
        long attacks = generatePawnAttacks(binStartSquare);

        long pawnTargets = singleTargets | doubleTargets | attacks;
        addPawnMoves(startSquare, pawnTargets);
    }

    private void addPawnMoves(int startSquare, long pawnTargets) {
        for (int target = 0; target < 64; target++) {
            // If there is a bit switched on
            if (pawnTargets << ~target < 0) {
                addMove(startSquare, target);
            }
        }
    }

    private void generateKnightMoves(int startSquare, boolean friendly) {
        long notABFile = 0xFCFCFCFCFCFCFCFCL;
        long notGHFile = 0x3F3F3F3F3F3F3F3FL;

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
            long targetSquare = getPositionFromBitboard(position & notFriendlyPieces);
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
            int targetSquare = getPositionFromBitboard(targetSquareBitboard);
            addMove(startSquare, targetSquare);
        }
    }

    private void generateSlidingMoves(int startSquare, int piece, boolean friendly) {
        int startDirIndex = isType(piece, BISHOP) ? 4 : 0;
        int endDirIndex = isType(piece, ROOK) ? 4 : 8;
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
                    if (isColour(pieceOnTargetSquare, friendlyColour) && !isType(pieceOnTargetSquare, KING)) {
                        moveBlocked = true;
                    }
                    tabooXRay = addBit(tabooXRay, targetSquare);
                    continue;
                }
                if (isColour(pieceOnTargetSquare, friendlyColour)) {
                    moveBlocked = true;
                }
                if (!moveBlocked) {
                    addMove(startSquare, targetSquare);
                }
                if (isColour(pieceOnTargetSquare, opponentColour)) {
                    moveBlocked = true;
                }
            }
        }
    }

    private long addBit(long bitboard, int position) {
        int root = position == 63 ? -2 : 2;
        if (position != 64) {
            return bitboard | (long) pow(root, position);
        }
        return bitboard;
    }

    private long subBit(long bitboard, int position) {
        int root = position == 63 ? -2 : 2;
        if (position != 64) {
            return bitboard - (long) pow(root, position);
        }
        return bitboard;
    }

    /**
     * If you provide a bitboard, this method returns the position of the lest significant bit
     *
     * @param bitboard the bitboard to query
     * @return the position of the least significant bit
     */
    private int getPositionFromBitboard(long bitboard) {
        return Long.numberOfTrailingZeros(bitboard);
    }

    private List<Integer> getPositionsFromBitboard(long bitboard) {
        List<Integer> positions = new ArrayList<>();
        while (bitboard > 0) {
            positions.add(getPositionFromBitboard(bitboard));
            bitboard = subBit(bitboard, positions.get(positions.size() - 1));
        }
        return positions;
    }

    private void addMove(int start, int target) {
        moves.add(new Move(start, target));
    }

    public long getPinned() {
        return pinnedPieces;
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
