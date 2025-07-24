package com.example.hexeditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchController {
    private final HexFileModel model;

    public SearchController(HexFileModel model) {
        this.model = model;
    }

    public List<Integer> searchWithMask(byte[] pattern, byte[] mask) throws IOException {
        List<Integer> result = new ArrayList<>();
        byte[] data = model.readAll();
        int dataLen = data.length;
        int patternLen = pattern.length;

        if (patternLen == 0 || dataLen < patternLen) return result;

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
                for (int j = 0; j < patternLen; j++) {
                    result.add(i + j);
                }
            }
        }

        return result;
    }

}
