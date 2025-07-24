package com.example.hexeditor;

import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HexTableModel extends AbstractTableModel {
    private static final String EMPTY_VALUE = "";
    private static final String ERROR_VALUE = "??";

    private final HexFileModel fileModel;
    private final int bytesPerRow;
    private List<Integer> highlightedOffsets = new ArrayList<>();

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
    public Object getValueAt(int row, int column) {
        try {
            if (isInvalidPosition(row, column)) {
                return EMPTY_VALUE;
            }
            byte byteValue = readByteAtPosition(row, column);
            return formatByteAsHex(byteValue);
        } catch (IOException e) {
            return ERROR_VALUE;
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
        if (!hexStr.matches("[0-9A-F]{1,2}")) {
            return;
        }

        try {
            int value = Integer.parseInt(hexStr, 16);
            long offset = (long) row * bytesPerRow + col;
            fileModel.writeByte(offset, (byte) value);
            fireTableCellUpdated(row, col);
        } catch (Exception e) {
            System.err.println("Ошибка при записи: " + e.getMessage());
        }
    }

    public void setHighlightedOffsets(List<Integer> offsets) {
        this.highlightedOffsets = new ArrayList<>(offsets);
        fireTableDataChanged();
    }

    public boolean isHighlighted(int offset) {
        return highlightedOffsets.contains(offset);
    }

    public void clearSearchHighlights() {
        highlightedOffsets.clear();
    }

    private boolean isInvalidPosition(int row, int column) throws IOException {
        long offset = calculateOffset(row, column);
        return offset >= fileModel.getLength();
    }

    private long calculateOffset(int row, int column) {
        return (long) row * bytesPerRow + column;
    }

    private byte readByteAtPosition(int row, int column) throws IOException {
        long offset = calculateOffset(row, column);
        return fileModel.readByte(offset);
    }

    private String formatByteAsHex(byte value) {
        return String.format("%02X", value);
    }
}
