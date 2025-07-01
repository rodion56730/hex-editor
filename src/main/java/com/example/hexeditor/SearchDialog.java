package com.example.hexeditor;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class SearchDialog extends JDialog {
    private final JTextField hexField;
    private final JTextField maskField;
    private boolean confirmed = false;

    public SearchDialog(JFrame parent) {
        super(parent, "Поиск байтов", true);
        setLayout(new GridLayout(3, 2, 5, 5));
        setSize(400, 150);
        setLocationRelativeTo(parent);

        add(new JLabel("HEX последовательность:"));
        hexField = new JTextField();
        add(hexField);

        add(new JLabel("Маска (необязательно):"));
        maskField = new JTextField();
        add(maskField);

        JButton searchButton = new JButton("Поиск");
        JButton cancelButton = new JButton("Отмена");

        searchButton.addActionListener(e -> {
            confirmed = true;
            setVisible(false);
        });

        cancelButton.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
        });

        add(searchButton);
        add(cancelButton);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public byte[] getHexBytes() {
        return parseHexString(hexField.getText());
    }

    public byte[] getMaskBytes() {
        String maskText = maskField.getText().trim();
        return maskText.isEmpty() ? null : parseHexString(maskText);
    }

    private byte[] parseHexString(String hex) {
        String[] parts = hex.trim().split("\\s+");
        byte[] result = new byte[parts.length];
        System.out.println(Arrays.toString(parts));
        System.out.println(parts.length);
        if (parts.length == 0) {
            return result;
        }
        for (int i = 0; i < parts.length; i++) {
            result[i] = (byte) Integer.parseInt(parts[i], 16);
        }

        return result;
    }
}
