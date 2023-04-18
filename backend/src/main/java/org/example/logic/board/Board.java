package org.example.logic.board;

import org.example.logic.MoveGenerator;
import org.example.logic.pieces.Piece;
import org.example.util.LoadUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static java.lang.Math.floor;
import static java.lang.String.format;
import static org.example.logic.pieces.Piece.*;
import static org.example.util.LoadUtil.loadSquaresFromFile;
import static org.example.util.MoveUtil.getStartSquare;
import static org.example.util.MoveUtil.getTargetSquare;


// Maybe make board a singleton
public class Board {
//    private static final Logger logger = LoggerFactory.getLogger(Board.class);
    private static final Queue<int[]> squareHistory = Collections.asLifoQueue(new ArrayDeque<>());
    private static final Queue<boolean[]> castlingHistory = Collections.asLifoQueue(new ArrayDeque<>());
    static boolean gameRunning = true;
    static int winningPiece;
    static String winReason;
    private static boolean[] whiteCastlingRights = new boolean[2];
    private static boolean[] blackCastlingRights = new boolean[2];
    private static int[] squares;
    private static int colourToMove = BLACK;
    private static List<String> moves;
    private static int friendlyKingPosition;
    private static int opponentKingPosition;
    private static List<EnPassantMove> enPassantMoves = new ArrayList<>();

    public Board() {

        squares = loadSquaresFromFile();

        getKingPositions();

        if (colourToMove == WHITE) {
            if (friendlyKingPosition == 4) {
                whiteCastlingRights = new boolean[]{squares[7] == (WHITE | ROOK), squares[0] == (WHITE | ROOK)};
            }
            if (opponentKingPosition == 60) {
                blackCastlingRights = new boolean[]{squares[56] == (BLACK | ROOK), squares[63] == (BLACK | ROOK)};
            }
        }

        if (colourToMove == BLACK) {
            if (opponentKingPosition == 4) {
//                System.out.println("T " + (squares[0] == (WHITE | ROOK)));
                whiteCastlingRights = new boolean[]{squares[7] == (WHITE | ROOK), squares[0] == (WHITE | ROOK)};
            }
            if (friendlyKingPosition == 60) {
                blackCastlingRights = new boolean[]{squares[56] == (BLACK | ROOK), squares[63] == (BLACK | ROOK)};
            }
        }
        moves = MoveGenerator.generateMoves();
    }

    public static int[]  move(String move) {
//        System.out.println(Arrays.toString(whiteCastlingRights));
        int startPos = getStartSquare(move);
        int targetPos = getTargetSquare(move);
        // Update history stack
        squareHistory.add(Arrays.copyOf(squares, squares.length));
        castlingHistory.add(Arrays.copyOf(whiteCastlingRights, 2));
        castlingHistory.add(Arrays.copyOf(blackCastlingRights, 2));

        boolean castled = false;
        // If the king moves, castling rights are removed for the current player
//        System.out.println(friendlyKingPosition);
        if (startPos == friendlyKingPosition) {
            castled = castle(targetPos);
            if (!castled) {
                switch (colourToMove) {
                    case WHITE:
                        whiteCastlingRights[0] = false;
                        whiteCastlingRights[1] = false;
                        break;
                    case BLACK:
                        blackCastlingRights[0] = false;
                        blackCastlingRights[1] = false;
                        break;
                }
            }
        }

        if (!castled) {
            int pieceToMove = squares[startPos];
            int takenPiece = squares[targetPos];
            // Move pieces
            squares[startPos] = 0;
            squares[targetPos] = pieceToMove;
            // Check castling rights
            if (isType(pieceToMove, ROOK)) {
                updateCastlingRights(startPos);
            }

            if (isType(pieceToMove, PAWN)) {
                int targetRank = (int) (floor(targetPos / 8f) + 1);
                // Check en passant moves from previous move
                processEnPassantMoves(startPos, targetPos, pieceToMove);
                // Then update from this move
                checkForEnPassantMoves(startPos, targetPos, targetRank);
                // We don't need to check what colour because pawns can't move backwards, so target will always be one of the edge ranks
                if (targetRank == 8 || targetRank == 1) {
//                    if (request.getPromotingTo() == null) {
//                        throw new TurnException("Promoting piece is not specified");
//                    }
                    promote(targetPos, move.substring(4, 5));
                }
            }

            if (takenPiece > 0) {
                // Check castling rights
                if (isType(takenPiece, ROOK)) {
                    updateCastlingRights(targetPos);
                }
//                System.out.println("+TAKE");
                //todo: remove piece on check
            }
        }

        colourToMove = Piece.getOppositeColour(colourToMove);
        getKingPositions();
        moves = MoveGenerator.generateMoves();
        return squares;
    }

    public static void undoMove() {
        squares = squareHistory.remove();
        colourToMove = getOppositeColour(colourToMove);
        getKingPositions();
        blackCastlingRights = castlingHistory.remove();
        whiteCastlingRights = castlingHistory.remove();

        moves = MoveGenerator.generateMoves();
    }

    private static void promote(int targetPos, String targetPiece) {
        // Will take data from the request with the piece type to promote to.
        switch (targetPiece) {
            case "r" :
                squares[targetPos] = colourToMove | ROOK;
                break;
            case "n" :
                squares[targetPos] = colourToMove | KNIGHT;
                break;
            case "b" :
                squares[targetPos] = colourToMove | BISHOP;
                break;
            case "q" :
                squares[targetPos] = colourToMove | QUEEN;
                break;
        }
    }

    private static void processEnPassantMoves(int startPos, int targetPos, int pieceToMove) {
        for (EnPassantMove move : enPassantMoves) {
            if (startPos == move.attackingPawnPos && targetPos == move.targetPawnPos) {
                squares[move.attackingPawnPos] = 0;
                squares[move.targetPawnPos] = pieceToMove;
                squares[move.attackedPawnPos] = 0;
//                logger.debug("En Passant take");
                //Todo: add taken piece to list
                break;
            }
        }
    }

    private static void checkForEnPassantMoves(int startPos, int targetPos, int targetRank) {
        enPassantMoves = new ArrayList<>();
        int startRank = (int) (floor(startPos / 8f) + 1);
        if (isColour(colourToMove, WHITE) && startRank != 2) {
            return;
        }
        if (isColour(colourToMove, BLACK) && startRank != 7) {
            return;
        }
        int targetFile = targetPos % 8 + 1;

        if (colourToMove == WHITE && targetRank == 4) {
            generateEnPassantMoves(targetPos, targetFile, BLACK);
            return;
        }
        if (colourToMove == BLACK && targetRank == 5) {
            generateEnPassantMoves(targetPos, targetFile, WHITE);
        }
    }

    // This is here for now because En Passant moves are generated after moves from the previous move are processed
    // Trying to think of a better solution
    private static void generateEnPassantMoves(int targetPos, int targetFile, int opponentColour) {
        int targetPawnPos = opponentColour == BLACK ? targetPos - 8 : targetPos + 8;
        if (targetFile - 1 >= 1) {
            int leftPiece = squares[targetPos - 1];
            if (isColour(leftPiece, opponentColour) && isType(leftPiece, PAWN)) {
                enPassantMoves.add(new EnPassantMove(targetPos - 1, targetPos, targetPawnPos));
            }
        }
        if (targetFile + 1 <= 8) {
            int rightPiece = squares[targetPos + 1];
            if (isColour(rightPiece, opponentColour) && isType(rightPiece, PAWN)) {
                enPassantMoves.add(new EnPassantMove(targetPos + 1, targetPos, targetPawnPos));
            }
        }
//        System.out.println(enPassantMoves.toString());
    }

    private static boolean castle(int targetPosition) {
//        System.out.println("target");
//        System.out.println(targetPosition);
        if ((friendlyKingPosition == 4 || friendlyKingPosition == 60)
                && (targetPosition == 2 || targetPosition == 6 || targetPosition == 58 || targetPosition == 62)) {
//            System.out.println("CASTLE");
            switch (friendlyKingPosition) {
                // If white king start position
                case 4:
                    switch (targetPosition) {
                        case 2:
                            squares[0] = 0;
                            squares[4] = 0;
                            squares[2] = WHITE | KING;
                            squares[3] = WHITE | ROOK;
                            break;
                        case 6:
                            squares[4] = 0;
                            squares[7] = 0;
                            squares[6] = WHITE | KING;
                            squares[5] = WHITE | ROOK;
                            break;
                    }
                    break;
                // If black king start position
                case 60:
                    switch (targetPosition) {
                        case 58:
                            squares[56] = 0;
                            squares[60] = 0;
                            squares[58] = BLACK | KING;
                            squares[59] = BLACK | ROOK;
                            break;
                        case 62:
                            squares[60] = 0;
                            squares[63] = 0;
                            squares[62] = BLACK | KING;
                            squares[61] = BLACK | ROOK;
                            break;
                    }
            }
            return true;
        }
        return false;
    }

    private static void getKingPositions() {
        for (int startSquare = 0; startSquare < 64; startSquare++) {
            int piece = squares[startSquare];
            if (isType(piece, KING) && isColour(piece, colourToMove)) {
                friendlyKingPosition = startSquare;
                continue;
            }
            if (isType(piece, KING) && isColour(piece, getOppositeColour(colourToMove))) {
                opponentKingPosition = startSquare;
            }
        }
    }

    public static void updateCastlingRights(int pos) {
        if (whiteCastlingRights[0] || whiteCastlingRights[1] || blackCastlingRights[0] || blackCastlingRights[1]) {
            switch (pos) {
                case 7:
                    whiteCastlingRights[0] = false;
                    break;
                case 0:
                    whiteCastlingRights[1] = false;
                    break;
                case 56:
                    blackCastlingRights[0] = false;
                    break;
                case 63:
                    blackCastlingRights[1] = false;
                    break;
            }
        }
    }

    public static List<String> getMoves() {
        return moves;
    }

    public static int[] getSquares() {
        return squares;
    }

    public static int getColourToMove() {
        return colourToMove;
    }

    public static long getTaboo() {
        return MoveGenerator.getTaboo();
    }

    public static void alertCheckmate() {
        gameRunning = false;
        winningPiece = colourToMove;
        winReason = "checkmate";
    }

    public static void alertCheck() {
        gameRunning = false;
        winningPiece = colourToMove;
        winReason = "check";
    }

    public static boolean[] getWhiteCastlingRights() {
        return whiteCastlingRights;
    }

    public static boolean[] getBlackCastlingRights() {
        return blackCastlingRights;
    }

    public static int getFriendlyKingPosition() {
        return friendlyKingPosition;
    }

    public static int getOpponentKingPosition() {
        return opponentKingPosition;
    }

    public static List<EnPassantMove> getEnPassantMoves() {
        return enPassantMoves;
    }

    public long getPinned() {
        return MoveGenerator.getPinned();
    }

    public long getTabooXRay() {
        return MoveGenerator.getTabooXRay();
    }

    public static class EnPassantMove {
        private final int attackingPawnPos;
        private final int attackedPawnPos;
        private final int targetPawnPos;

        /**
         * @param attackingPawnPos the position of the pawn that could attack
         * @param attackedPawnPos  the position fo the pawn that is being attacked
         * @param targetPawnPos    where the attacking pawn will end up
         */
        public EnPassantMove(int attackingPawnPos, int attackedPawnPos, int targetPawnPos) {
            this.attackingPawnPos = attackingPawnPos;
            this.attackedPawnPos = attackedPawnPos;
            this.targetPawnPos = targetPawnPos;
        }

        public int getAttackingPawnPos() {
            return attackingPawnPos;
        }

        public int getTargetPawnPos() {
            return targetPawnPos;
        }

        public int getAttackedPawnPos() {
            return attackedPawnPos;
        }

        @Override
        public String toString() {
            return "EnPassantMove{" +
                    "attackingPawnPos=" + attackingPawnPos +
                    ", attackedPawnPos=" + attackedPawnPos +
                    ", targetPawnPos=" + targetPawnPos +
                    '}';
        }
    }
}