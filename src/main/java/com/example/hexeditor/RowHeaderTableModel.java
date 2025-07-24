package com.example.hexeditor;

import javax.swing.table.AbstractTableModel;

public class RowHeaderTableModel extends AbstractTableModel {
    private final int rowCount;
    private final int bytesPerRow;

    public RowHeaderTableModel(int rowCount, int bytesPerRow) {
        this.rowCount = rowCount;
        this.bytesPerRow = bytesPerRow;
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return String.format("%08X", rowIndex * bytesPerRow);
    }
}
