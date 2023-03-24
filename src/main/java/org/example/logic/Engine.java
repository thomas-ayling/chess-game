package org.example.logic;

import org.example.logic.board.Board;

import java.util.List;

public class Engine {

    public static int search(int depth) {
        if (depth == 0) {
            return 1;
        }

//        List<MoveGenerator.Move> moves = MoveGenerator.generateMoves();
        List<MoveGenerator.Move> moves = Board.getMoves();

        int possibleMoves = 0;
        for (MoveGenerator.Move move : moves) {
            Board.move(move.startSquare, move.targetSquare);
            possibleMoves += search(depth - 1);
            Board.undoMove();
        }
        return possibleMoves;
    }
}
