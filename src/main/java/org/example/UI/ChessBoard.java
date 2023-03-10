package org.example.UI;

import org.example.logic.Board;
import org.example.logic.MoveGenerator;
import org.example.logic.MoveGenerator.Move;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.java.org.example.UI.Constants.*;
import static org.example.logic.MoveGenerator.*;
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

    boolean[] selectedSquare;
    List<Move> moves;

    List<Move> selectedMoves = new ArrayList<>();

    Board board;

    public ChessBoard() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setVisible(true);
        setFocusable(true);
        setBackground(Color.lightGray);
        addMouseListener(new Adapter());

        board = new Board();

        selectedSquare = new boolean[64];

        moves = board.getMoves();

        timer.start();
    }

    public void setup() {
    }

    public void drawBoard(Graphics g, int[] squares) {
        int offset = 12;
        int size = 50;
        Graphics2D g2 = (Graphics2D) g.create();

        // Draw board
        g2.setStroke(new java.awt.BasicStroke(3));
        g2.setColor(Color.black);
        for (int file = 0; file < CELLS; file++) {
            for (int rank = 0; rank < CELLS; rank++) {
                if ((file + rank) % 2 == 0) {
                    g2.setColor(Color.black);
                    g2.fillRect(BOARD_HEIGHT - CELL_SIZE - file * CELL_SIZE, BOARD_HEIGHT - CELL_SIZE - (rank * CELL_SIZE), CELL_SIZE, CELL_SIZE);
                }
                if (String.format("%64s", Long.toBinaryString(board.getTaboo())).replace(' ', '0').charAt(file + (rank * 8)) == '1') {
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
                if (selectedSquare[currentPosition] && squares[currentPosition] != 0) {
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
        g.translate(BOARD_OFFSET, BOARD_OFFSET);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        g.setColor(Color.black);
        drawBoard(g, board.getSquares());
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
        setup();
        move();
        repaint();
    }

    private class Adapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            int mouseX = e.getX();
            int mouseY = e.getY();
            if (mouseX > BOARD_OFFSET && mouseX < BOARD_WIDTH + BOARD_OFFSET && mouseY > BOARD_OFFSET && mouseY < BOARD_HEIGHT + BOARD_OFFSET) {
                selectedSquare = new boolean[64];
                int file = Math.floorDiv(mouseX - BOARD_OFFSET, CELL_SIZE);
                int rank = Math.floorDiv(mouseY - BOARD_OFFSET, CELL_SIZE);
                selectedSquare[(rank * 8) + file] = true;
                selectedMoves = new ArrayList<>();
                for (Move move : moves) {
                    if (move.startSquare == (rank * 8) + file) {
                        selectedMoves.add(move);
                    }
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            super.mouseEntered(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            super.mouseExited(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
        }
    }
}
