package org.example.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Logger logger = LoggerFactory.getLogger(MoveGenerator.class);
    private long pinnedPieces = 0;
    private long empty;
    private long notWhite;
    private long notBlack;
    private long notFriendlyPieces;
    private long taboo;
    private long tabooXRay;
    private List<Move> moves;
    private List<Move> kingMoves;
    private List<Move> checkingMoves;
    private int friendlyColour;
    private int opponentColour;
    private int friendlyKingPosition;
    private int opponentKingPosition;
    private int[] squares;
    private boolean check = false;

    private static long northOne(long bit) {
        return bit << 8;
    }

    private long northEastOne(long bit) {
        return (bit << 9) & notAFile;
    }

    private long eastOne(long bit) {
        return (bit << 1) & notAFile;
    }

    private long southEastOne(long bit) {
        return (bit >> 7) & notAFile;
    }

    private long southOne(long bit) {
        return bit >> 8;
    }

    private long southWestOne(long bit) {
        return (bit >> 9) & notHFile;
    }

    private long westOne(long bit) {
        return (bit >> 1) & notHFile;
    }

    private long northWestOne(long bit) {
        return (bit << 7) & notHFile;
    }

    public List<Move> generateMoves(Board board) {
        resetAttributes(board);
        generateBitboards();

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
                }
            }
            generateKingMoves(friendly);
            if (friendly) {
                generateCastleMoves(board.getWhiteCastlingRights(), board.getBlackCastlingRights());
            }
        }

        checkKingLegality();
        generateEnPassantMoves(board.getEnPassantMoves());

        return moves;
    }

    private void generateEnPassantMoves(List<Board.EnPassantMove> possibleEnPassantMoves) {
        if (possibleEnPassantMoves.size() == 0) {
            return;
        }
        for (Board.EnPassantMove move : possibleEnPassantMoves) {
            long startPosBitboard = addBit(0, move.getAttackingPawnPos());
            if ((startPosBitboard & pinnedPieces) == 0) {
                addMove(move.getAttackingPawnPos(), move.getTargetPawnPos());
            }
        }
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

    private void generatePinnedPieceMoves() {
        List<Integer> pinnedPiecePositions = getPositionsFromBitboard(pinnedPieces);

        for (int position : pinnedPiecePositions) {
            int pinnedPiece = squares[position];

            int xDist = (position % 8) - (friendlyKingPosition % 8);
            int yDist = (int) ((int) floor(position / 8f) - floor(friendlyKingPosition / 8f));

            int directionToKing = getDirectionToTarget(xDist, yDist);
            int directionToPinningPiece = getOppositeDirection(directionToKing);

            int distanceToKing = (int) sqrt(pow(xDist, 2) + pow(yDist, 2));

            if (isType(pinnedPiece, PAWN)) {
                List<Move> movesToRemove = new ArrayList<>();
                for (Move move : moves) {
                    // if (positions is not start of a possible move) or (move attacks the pinning piece) then check next move, otherwise remove the move
                    if (move.startSquare != position || directionToPinningPiece == 0 || directionToPinningPiece == 1) {
                        continue;
                    }
                    movesToRemove.add(move);
                }
                moves.removeAll(movesToRemove);
                continue;
            }

            removeMoves(position);

            // If a knight is pinned it cannot move, so we move on to the next pinned piece if any
            if (isType(pinnedPiece, KNIGHT)) {
                continue;
            }
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
    private int getDirectionToTarget(int xDist, int yDist) {
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

    private void resetAttributes(Board board) {
        squares = board.getSquares();
        friendlyColour = board.getColourToMove();
        opponentColour = getOppositeColour(friendlyColour);
        moves = new ArrayList<>();
        kingMoves = new ArrayList<>();
        checkingMoves = new ArrayList<>();
        friendlyKingPosition = board.getFriendlyKingPosition();
        opponentKingPosition = board.getOpponentKingPosition();
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

    private void checkKingLegality() {
        long kingPositionBitboard = addBit(0, friendlyKingPosition);

        check = (kingPositionBitboard & taboo) >= 1;
        if (check) {
            moves = getRemainingLegalMoves();
            if (moves.size() == 0) {
                System.out.println("CHECKMATE");
            }
            System.out.println("CHECK");
            return;
        }
        if (moves.size() == 0) {
            System.out.println("STALEMATE");
            return;
        }
        checkForPinnedPieces(kingPositionBitboard);
        generatePinnedPieceMoves();
    }

    private List<Move> getRemainingLegalMoves() {
        if (checkingMoves.size() > 1) {
            return kingMoves;
        }

        // Checkmate is true unless a move can cover the check, or the checking piece can be taken
        List<Move> remainingLegalMoves = new ArrayList<>();
        Move checkingMove = checkingMoves.get(0);
        int xDist = (checkingMove.startSquare % 8) - (friendlyKingPosition % 8);
        int yDist = (int) floor(friendlyKingPosition / 8f - floor(checkingMove.startSquare) / 8f);


        int checkDirection = directionOffsets[getDirectionToTarget(xDist, yDist)];
        int checkDistance = (int) sqrt(pow(xDist, 2) + pow(yDist, 2));

        List<Integer> interceptingTargets = new ArrayList<>();

        for (int i = 1; i <= checkDistance; i++) {
            interceptingTargets.add(checkingMove.startSquare + (checkDirection * i));
        }

        for (Move friendlyMove : moves) {
            if (friendlyMove.targetSquare == checkingMove.startSquare || interceptingTargets.contains(friendlyMove.targetSquare)) {
                remainingLegalMoves.add(friendlyMove);
            }
        }
        remainingLegalMoves.addAll(kingMoves);
        return remainingLegalMoves;
    }

    private void checkForPinnedPieces(long kingPositionBitboard) {
        if ((kingPositionBitboard & tabooXRay) == 0) {
            return;
        }

        // Sliding logic sends rays out from the king
        for (int directionIndex = 0; directionIndex < 8; directionIndex++) {
            List<Integer> potentialPinnedPiecePositions = new ArrayList<>();
            // For each square stemming from the king position in certain direction
            for (int n = 0; n < numSquaresToEdge[friendlyKingPosition][directionIndex]; n++) {
                int targetSquare = friendlyKingPosition + directionOffsets[directionIndex] * (n + 1);
                int pieceOnTargetSquare = squares[targetSquare];

                if (pieceOnTargetSquare == 0) {
                    continue;
                }
                if (potentialPinnedPiecePositions.size() > 1) {
                    break;
                }
                if (isSlidingPiece(pieceOnTargetSquare) && isColour(pieceOnTargetSquare, opponentColour)) {
                    if (potentialPinnedPiecePositions.size() > 0) {
                        if (isType(pieceOnTargetSquare, ROOK) && directionIndex > 3) {
                            break;
                        }
                        if (isType(pieceOnTargetSquare, BISHOP) && directionIndex < 4) {
                            break;
                        }
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
            for (int attack : getPositionsFromBitboard(attacks)) {
                if (attack == friendlyKingPosition) {
                    checkingMoves.add(new Move(startSquare, attack));
                }
            }
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
            int targetSquare = getPositionFromBitboard(position & notFriendlyPieces);

            if (!friendly) {
                taboo |= position;
                if (targetSquare == friendlyKingPosition) {
                    checkingMoves.add(new Move(startSquare, targetSquare));
                }
                continue;
            }
            addMove(startSquare, targetSquare);
        }
    }

    private void generateKingMoves(boolean friendly) {

        long binStartSquare = addBit(0, friendly ? friendlyKingPosition : opponentKingPosition);
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
            addMove(friendlyKingPosition, targetSquare);
            addKingMove(friendlyKingPosition, targetSquare);
        }
    }

    private void generateCastleMoves(boolean[] whiteCastlingRights, boolean[] blackCastlingRights) {
        if (friendlyColour == WHITE) {
            if (whiteCastlingRights[0] && squares[5] == 0 && squares[6] == 0) {
                long involvedSquares = addBit(addBit(addBit(0, 6), 5), 4);
                if ((involvedSquares & taboo) == 0) {
                    addMove(friendlyKingPosition, 6);
                }
            }
            if (whiteCastlingRights[1] && squares[3] == 0 && squares[2] == 0 && squares[1] == 0) {
                long involvedSquares = addBit(addBit(addBit(0, 4), 3), 2);
                if ((involvedSquares & taboo) == 0) {
                    addMove(friendlyKingPosition, 2);
                }
            }
            return;
        }
        if (friendlyColour == BLACK) {
            if (blackCastlingRights[0] && squares[57] == 0 && squares[58] == 0 && squares[59] == 0) {
                long involvedSquares = addBit(addBit(addBit(0, 58), 59), 60);
                if ((involvedSquares & taboo) == 0) {
                    addMove(friendlyKingPosition, 58);
                }
            }
            if (blackCastlingRights[1] && squares[61] == 0 && squares[62] == 0) {
                long involvedSquares = addBit(addBit(addBit(0, 60), 61), 62);
                if ((involvedSquares & taboo) == 0) {
                    addMove(friendlyKingPosition, 62);
                }
            }
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
                        if (targetSquare == friendlyKingPosition) {
                            checkingMoves.add(new Move(startSquare, targetSquare));
                        }
                    }
                    if (pieceOnTargetSquare > 0 && !isType(pieceOnTargetSquare, KING)) {
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
        if (target < 0 || target > 63) {
            return;
        }
        moves.add(new Move(start, target));
    }

    private void addKingMove(int start, int target) {
        if (target < 0 || target > 63) {
            return;
        }
        kingMoves.add(new Move(start, target));
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

        @Override
        public String toString() {
            return "Move{" +
                    "startSquare=" + startSquare +
                    ", targetSquare=" + targetSquare +
                    '}';
        }
    }
}
