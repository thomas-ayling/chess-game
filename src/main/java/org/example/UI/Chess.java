package org.example.UI;

import org.example.util.ByteUtil;

import javax.swing.*;
import java.awt.*;

public class Chess extends JFrame {

    public Chess() {
        add(new ChessBoard());
        pack();
        setTitle("Chess");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JFrame frame = new Chess();
            frame.setVisible(true);
        });
    }
}