package com.example.hexeditor;

import javax.swing.*;
import java.awt.*;

public class EditDialog extends JDialog {
    private final JTextField hexField;
    private final JCheckBox overwriteBox;
    private boolean confirmed = false;

    public EditDialog(JFrame parent, String title) {
        super(parent, title, true);
        setLayout(new GridLayout(3, 1));
        setSize(300, 150);
        setLocationRelativeTo(parent);

        hexField = new JTextField();
        overwriteBox = new JCheckBox("Режим замены");

        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Отмена");

        ok.addActionListener(e -> {
            confirmed = true;
            setVisible(false);
        });

        cancel.addActionListener(e -> setVisible(false));

        add(new JLabel("Введите HEX байты (например: DE AD BE EF):"));
        add(hexField);
        add(overwriteBox);

        JPanel panel = new JPanel();
        panel.add(ok);
        panel.add(cancel);
        add(panel);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public byte[] getBytes() {
        String[] parts = hexField.getText().trim().split("\\s+");
        byte[] result = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = (byte) Integer.parseInt(parts[i], 16);
        }
        return result;
    }

    public boolean isOverwrite() {
        return overwriteBox.isSelected();
    }
}
