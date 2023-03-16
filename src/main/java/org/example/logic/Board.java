package org.example.logic;

import org.example.util.LoadUtil;
import org.example.logic.pieces.Piece;
import org.example.logic.MoveGenerator.Move;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public class Board {
    private int[] squares;
    private int colourToMove = Piece.BLACK;
    private List<Move> moves;
    private final MoveGenerator moveGenerator = new MoveGenerator();

    public Board() {
        try {
            File fenFile = new File("src/main/resources/pinned-piece-board.fen");
            Scanner scanner = new Scanner(fenFile);
            String fenString = scanner.next();
            squares = LoadUtil.LoadBoardFromFen(fenString);
            System.out.println(fenString);
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        moves = moveGenerator.generateMoves(this);
    }

    public void move(int startPos, int targetPos) {
        int pieceToMove = squares[startPos];
        int takenPiece = squares[targetPos];
        squares[startPos] = 0;
        squares[targetPos] = pieceToMove;
        if (takenPiece > 0) {
            //todo: remove piece on check
            System.out.println(Piece.getType(takenPiece));
        }
        colourToMove = Piece.getOppositeColour(colourToMove);
        moves = moveGenerator.generateMoves(this);
    }

    public int[] getSquares() {
        return squares;
    }

    public int getColourToMove() {
        return colourToMove;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public long getPinned() {
        return moveGenerator.getPinned();
    }
    public long getTabooXRay() {
        return moveGenerator.getTabooXRay();
    }
    public long getTaboo() {
        return moveGenerator.getTaboo();
    }
}