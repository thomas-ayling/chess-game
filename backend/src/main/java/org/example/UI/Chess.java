package org.example.UI;

import org.example.logic.board.Board;

import javax.swing.*;
import java.awt.*;

import static org.example.util.PrecomputedMoveData.squareMap;

public class Chess extends JFrame {

    public Chess() {
        add(new ChessBoard());
        pack();
        setTitle("Chess");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        String a = "a1b2";

        System.out.println((squareMap.indexOf(a.substring(0, 2))));


        System.out.println((squareMap.indexOf(a.substring(2, 4))));

    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JFrame frame = new Chess();
            frame.setVisible(true);
        });
    }
}