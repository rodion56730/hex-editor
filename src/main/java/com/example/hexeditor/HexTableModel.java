package com.example.hexeditor;

import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HexTableModel extends AbstractTableModel {
    private final HexFileModel fileModel;
    private final int bytesPerRow;

    public HexTableModel(HexFileModel model, int bytesPerRow) {
        this.fileModel = model;
        this.bytesPerRow = bytesPerRow;
    }


    @Override
    public int getRowCount() {
        try {
            return (int) ((fileModel.getLength() + bytesPerRow - 1) / bytesPerRow);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getColumnCount() {
        return bytesPerRow;
    }

    @Override
    public Object getValueAt(int row, int col) {
        try {
            long offset = (long) row * bytesPerRow + col;
            if (offset >= fileModel.getLength()) return "";
            byte b = fileModel.readByte(offset);
            return String.format("%02X", b);
        } catch (IOException e) {
            return "??";
        }
    }

    @Override
    public String getColumnName(int col) {
        return String.format("+%X", col);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        long offset = (long) row * bytesPerRow + col;
        try {
            return offset < fileModel.getLength();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void setValueAt(Object aValue, int row, int col) {
        String hexStr = aValue.toString().trim().toUpperCase();
        if (!hexStr.matches("[0-9A-F]{1,2}")) return;

        try {
            int value = Integer.parseInt(hexStr, 16);
            long offset = (long) row * bytesPerRow + col;
            fileModel.writeByte(offset, (byte) value);
            fireTableCellUpdated(row, col);
        } catch (Exception e) {
            System.err.println("Ошибка при записи: " + e.getMessage());
        }
    }

    private List<Integer> highlightedOffsets = new ArrayList<>();

    public void setHighlightedOffsets(List<Integer> offsets) {
        this.highlightedOffsets = offsets;

        fireTableDataChanged();
    }

    public boolean isHighlighted(int offset) {
        return highlightedOffsets.contains(offset);
    }

    public void clearSearchHighlights() {
        highlightedOffsets.clear();
    }

}
