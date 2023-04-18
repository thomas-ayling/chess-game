package org.example.logic;

import org.example.logic.board.Board;
import org.example.util.LoadUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.example.util.MoveUtil.getStartSquare;
import static org.example.util.MoveUtil.getTargetSquare;

public class Engine {

    public static int search(int depth, String previousMove) {
        List<String> moves = Board.getMoves();

        if (depth == 1) {
//            System.out.println(previousMove + ": " + moves.size());
            return moves.size();
        }

        int possibleMoves = 0;
        for (String move : moves) {
            Board.move(move);
            possibleMoves += search(depth - 1, move);
//            System.out.println(LoadUtil.loadFenFromBoard(Board.getSquares()));
            Board.undoMove();
        }
        if (depth == 2) {
            System.out.println(previousMove + ": " + possibleMoves);
        }

        return possibleMoves;
    }
}
