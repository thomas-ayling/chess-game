package org.example.UI;

import org.example.logic.Engine;
import org.example.logic.MoveGenerator.Move;
import org.example.logic.board.Board;
import org.example.logic.pieces.Piece;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import static main.java.org.example.UI.Constants.*;
import static org.example.logic.pieces.Piece.*;

public class ChessBoard extends JPanel implements ActionListener {

    private final Timer timer = new Timer(30, this);
    private final Map<Integer, Image> pieceMap = new HashMap<>() {{
        put(KING | WHITE, new ImageIcon("src/main/java/org/example/assets/white-king.png").getImage());
        put(PAWN | WHITE, new ImageIcon("src/main/java/org/example/assets/white-pawn.png").getImage());
        put(KNIGHT | WHITE, new ImageIcon("src/main/java/org/example/assets/white-knight.png").getImage());
        put(BISHOP | WHITE, new ImageIcon("src/main/java/org/example/assets/white-bishop.png").getImage());
        put(ROOK | WHITE, new ImageIcon("src/main/java/org/example/assets/white-rook.png").getImage());
        put(QUEEN | WHITE, new ImageIcon("src/main/java/org/example/assets/white-queen.png").getImage());
        put(KING | BLACK, new ImageIcon("src/main/java/org/example/assets/black-king.png").getImage());
        put(PAWN | BLACK, new ImageIcon("src/main/java/org/example/assets/black-pawn.png").getImage());
        put(KNIGHT | BLACK, new ImageIcon("src/main/java/org/example/assets/black-knight.png").getImage());
        put(BISHOP | BLACK, new ImageIcon("src/main/java/org/example/assets/black-bishop.png").getImage());
        put(ROOK | BLACK, new ImageIcon("src/main/java/org/example/assets/black-rook.png").getImage());
        put(QUEEN | BLACK, new ImageIcon("src/main/java/org/example/assets/black-queen.png").getImage());
    }};
    int selectedStartSquare;
    List<Move> moves;
    List<Move> selectedMoves = new ArrayList<>();
    boolean pieceIsSelected = false;

    public ChessBoard() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setVisible(true);
        setFocusable(true);
        setBackground(Color.lightGray);
        addMouseListener(new MouseAdapterImpl());
        addKeyListener(new KeyAdapterImpl());

        selectedStartSquare = -1;

        moves = Board.getMoves();

        timer.start();
        System.out.println("Engine result:");
        System.out.println(Engine.search(5));
    }

    public void drawBoard(Graphics2D g2, int[] squares) {
        int offset = 12;
        int size = 50;

        // Draw board
        g2.setStroke(new java.awt.BasicStroke(3));
        g2.setColor(Color.black);
        for (int file = 0; file < CELLS; file++) {
            for (int rank = 0; rank < CELLS; rank++) {
                if ((file + rank) % 2 == 0) {
                    g2.setColor(Color.black);
                    g2.fillRect(BOARD_HEIGHT - CELL_SIZE - file * CELL_SIZE, BOARD_HEIGHT - CELL_SIZE - (rank * CELL_SIZE), CELL_SIZE, CELL_SIZE);
                }
                if (String.format("%64s", Long.toBinaryString(Board.getTaboo())).replace(' ', '0').charAt(file + (rank * 8)) == '1') {
                    g2.setColor(Color.red);
                    g2.fillRect(BOARD_HEIGHT - CELL_SIZE - file * CELL_SIZE, BOARD_HEIGHT - CELL_SIZE - (rank * CELL_SIZE), CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // Draw selected square and pieces
        g2.setColor(Color.RED);
        for (int rank = 0; rank < CELLS; rank++) {
            for (int file = 0; file < CELLS; file++) {
                int currentPosition = file + (rank * 8);
                if (selectedStartSquare == currentPosition && squares[currentPosition] != 0) {
                    g2.drawRect(file * CELL_SIZE, rank * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
                if (squares[currentPosition] == 0) {
                    continue;
                }
                g2.drawImage(pieceMap.get(squares[currentPosition]), file * CELL_SIZE + offset, rank * CELL_SIZE + offset, size, size, null);
            }
        }

        // Draw possible moves
        for (int rank = 0; rank < CELLS; rank++) {
            for (int file = 0; file < CELLS; file++) {
                int currentPosition = file + (rank * 8);
                g2.setColor(new Color(20, 167, 204, 200));
                for (Move move : selectedMoves) {
                    if (move.targetSquare == currentPosition) {
                        g2.fillOval(file * CELL_SIZE + offset + 10, rank * CELL_SIZE + offset + 10, 30, 30);
                    }
                }
            }
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.translate(BOARD_OFFSET, BOARD_OFFSET);
//        g2.rotate(Math.toRadians(180));
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        g2.setColor(Color.black);
        drawBoard(g2, Board.getSquares());
    }

    public void move() {
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        move();
        repaint();
    }

    private class MouseAdapterImpl extends MouseAdapter {
        @Override
        public void mouseReleased(MouseEvent e) {
            int mouseX = e.getX();
            int mouseY = e.getY();
            if (mouseX > BOARD_OFFSET && mouseX < BOARD_WIDTH + BOARD_OFFSET && mouseY > BOARD_OFFSET && mouseY < BOARD_HEIGHT + BOARD_OFFSET) {
                int file = Math.floorDiv(mouseX - BOARD_OFFSET, CELL_SIZE);
                int rank = Math.floorDiv(mouseY - BOARD_OFFSET, CELL_SIZE);
                if (pieceIsSelected) {
                    int selectedTargetSquare = (rank * 8) + file;
                    for (Move move : moves) {
                        if (move.startSquare == selectedStartSquare && move.targetSquare == selectedTargetSquare) {
                            Board.move(move.startSquare, move.targetSquare);
                            pieceIsSelected = false;
                            selectedStartSquare = -1;
                            selectedMoves = new ArrayList<>();
                            moves = Board.getMoves();
                            return;
                        }
                    }
                }

//                System.out.println("MOUSE RELEASED");

                selectedStartSquare = (rank * 8) + file;
                if (Piece.isColour(Board.getSquares()[selectedStartSquare], Board.getColourToMove())) {
                    selectedMoves = new ArrayList<>();
                    for (Move move : moves) {
                        if (move.startSquare == selectedStartSquare) {
                            selectedMoves.add(move);
                        }
                    }
                    pieceIsSelected = true;
                    return;
                }
            }
            pieceIsSelected = false;
        }
    }

    private static class KeyAdapterImpl extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    Board.undoMove();
                    break;
                case KeyEvent.VK_RIGHT:
                    break;
            }
        }
    }
}
