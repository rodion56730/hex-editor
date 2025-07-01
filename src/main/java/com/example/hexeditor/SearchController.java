package com.example.hexeditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchController {
    private final HexFileModel model;
    private final HexTableModel tableModel;

    public SearchController(HexFileModel model, HexTableModel tableModel) {
        this.model = model;
        this.tableModel = tableModel;
    }

    public void search(byte[] pattern) {
            List<Integer> foundPositions = new ArrayList<>();
            try {
                byte[] data = model.readAll();
                for (int i = 0; i <= data.length-1; i++) {
                        byte dataByte = data[i];
                        byte patternByte = pattern[0];



                        if (dataByte == patternByte) {
                                foundPositions.add(i);
                        }
                }
                System.out.println("Found " + foundPositions);

                tableModel.setHighlightedOffsets(foundPositions);

            } catch (IOException e) {
                System.err.println("Ошибка поиска: " + e.getMessage());
            }
    }

    public void searchWithMask( byte[] pattern, byte[] mask) throws IOException {
        List<Integer> result = new ArrayList<>();
        byte[] data = model.readAll();
        int dataLen = data.length;
        int patternLen = pattern.length;

        if (patternLen == 0 || dataLen < patternLen) return;

        for (int i = 0; i <= dataLen - patternLen; i++) {
            boolean match = true;

            for (int j = 0; j < patternLen; j++) {
                byte dataByte = data[i + j];
                byte patternByte = pattern[j];
                byte maskByte = (mask != null && mask.length == patternLen) ? mask[j] : (byte) 0xFF;

                if ((dataByte & maskByte) != (patternByte & maskByte)) {
                    match = false;
                    break;
                }
            }

            if (match) {
                for(int j = 0; j < patternLen; j++) {
                    result.add(i+j);
                }

            }
        }

        tableModel.setHighlightedOffsets(result);
    }

}
