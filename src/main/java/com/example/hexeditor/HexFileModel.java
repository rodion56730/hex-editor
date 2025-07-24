package com.example.hexeditor;


import java.io.*;

public class HexFileModel {
    private final RandomAccessFile file;

    public HexFileModel(File f) throws IOException {
        this.file = new RandomAccessFile(f, "rw");
    }

    public byte readByte(long position) throws IOException {
        file.seek(position);
        return file.readByte();
    }

    public void writeByte(long position, byte value) throws IOException {
        file.seek(position);
        file.writeByte(value);
    }

    public long getLength() throws IOException {
        return file.length();
    }

    public void close() throws IOException {
        file.close();
    }

    public void deleteBytes(long position, int length, boolean shift) throws IOException {
        long fileLength = getLength();

        if (position + length > fileLength)
            length = (int) (fileLength - position);

        if (shift) {
            for (long i = position + length; i < fileLength; i++) {
                file.seek(i);
                byte b = file.readByte();
                file.seek(i - length);
                file.writeByte(b);
            }
            file.setLength(fileLength - length);
        } else {
            for (int i = 0; i < length; i++) {
                file.seek(position + i);
                file.writeByte(0);
            }
        }
    }

    public byte[] readAll() throws IOException {
        long length = getLength();
        if (length > Integer.MAX_VALUE) {
            throw new IOException("Файл слишком большой для загрузки в память.");
        }

        byte[] buffer = new byte[(int) length];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = readByte(i);
        }
        return buffer;
    }


    public void insertBytes(long position, byte[] bytes, boolean overwrite) throws IOException {
        long fileLength = getLength();
        int len = bytes.length;

        if (!overwrite) {
            for (long i = fileLength - 1; i >= position; i--) {
                file.seek(i);
                byte b = file.readByte();
                file.seek(i + len);
                file.writeByte(b);
            }
            file.setLength(fileLength + len);
        }

        file.seek(position);
        for (byte b : bytes) {
            file.writeByte(b);
        }
    }
    public byte[] readBlock(long offset, int length) throws IOException {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            if (offset + i < getLength()) {
                data[i] = readByte(offset + i);
            } else {
                data[i] = 0;
            }
        }
        return data;
    }
}

