package com.example.hexeditor;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = chooser.getSelectedFile();
                    HexEditorFrame frame = new HexEditorFrame(selectedFile);
                    frame.setVisible(true);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Ошибка: " + e.getMessage());
            }
        });
    }
}
