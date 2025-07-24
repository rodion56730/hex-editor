package com.example.hexeditor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Модель для работы с файлами в HEX-редакторе.
 * Обеспечивает чтение/запись байт, вставку, удаление и другие операции.
 */
public class HexFileModel {
    private final RandomAccessFile file;
    private static final Logger logger = LoggerFactory.getLogger(HexFileModel.class);

    /**
     * Открывает файл для чтения и записи.
     * @param f файл для работы
     * @throws IOException если файл не существует или недоступен
     */
    public HexFileModel(File f) throws IOException {
        this.file = new RandomAccessFile(f, "rw");
        logger.info("Файл открыт: {} (размер: {} байт)", f.getAbsolutePath(), file.length());
    }

    /**
     * Читает байт по указанной позиции.
     * @param position позиция в файле (в байтах)
     * @return прочитанный байт или 0, если позиция некорректна
     */
    public byte readByte(long position) throws IOException {
        if(position >= 0) {
            file.seek(position);
            byte value = file.readByte();
            logger.trace("Прочитан байт [{}] = 0x{}", position, String.format("%02X", value));
            return file.readByte();
        }
        logger.warn("Попытка чтения за пределами файла: position={}", position);
        return 0;
    }

    /**
     * Записывает байт в указанную позицию.
     * @param position позиция в файле
     * @param value значение байта
     */
    public void writeByte(long position, byte value) throws IOException {
        if(position >= 0 ) {
            file.seek(position);
            file.writeByte(value);
            logger.debug("Записан байт [{}] = 0x{}", position, String.format("%02X", value));
        } else {
            logger.warn("Попытка записи за пределами файла: position={}", position);
        }
    }

    public long getLength() throws IOException {
        return file.length();
    }

    public void close() throws IOException {
        file.close();
    }

    /**
     * Удаляет блок байт.
     * @param position начальная позиция
     * @param length количество байт
     * @param shift если true - сдвигает оставшиеся данные
     */
    public void deleteBytes(long position, int length, boolean shift) throws IOException {
        long fileLength = getLength();
        logger.info("Удаление {} байт с позиции {} (со сдвигом: {})", length, position, shift);

        if (position + length > fileLength) {
            logger.warn("Позиция удаления за пределами файла: {}", position);
            length = (int) (fileLength - position);
            logger.debug("Скорректированная длина удаления: {}", length);
        }

        if (shift) {
            logger.debug("Сдвиг данных на {} байт", length);
            for (long i = position + length; i < fileLength; i++) {
                file.seek(i);
                byte b = file.readByte();
                file.seek(i - length);
                file.writeByte(b);
            }
            file.setLength(fileLength - length);
        } else {
            logger.debug("Обнуление {} байт", length);
            for (int i = 0; i < length; i++) {
                file.seek(position + i);
                file.writeByte(0);
            }
        }
    }

    /**
     * Читает весь файл в память.
     * @throws IOException если файл слишком большой
     */
    public byte[] readAll() throws IOException {
        long length = getLength();
        if (length > Integer.MAX_VALUE) {
            logger.error("Файл слишком большой: {} байт", length);
            throw new IOException("Файл слишком большой для загрузки в память.");
        }

        logger.debug("Чтение всего файла ({} байт)", length);
        byte[] buffer = new byte[(int) length];
        for (int i = 0; i < buffer.length; i++) {
            try {
                buffer[i] = readByte(i);
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
        return buffer;
    }

    /**
     * Вставляет массив байт.
     * @param position позиция вставки
     * @param bytes данные для вставки
     * @param overwrite true - перезаписать, false - вставить со сдвигом
     */
    public void insertBytes(long position, byte[] bytes, boolean overwrite) throws IOException {
        long fileLength = getLength();
        int len = bytes.length;
        logger.info("Вставка {} байт с позиции {} (перезапись: {})",
                bytes.length, position, overwrite);

        if (!overwrite) {
            for (long i = fileLength - 1; i >= position; i--) {
                file.seek(i);
                byte b = file.readByte();
                file.seek(i + len);
                file.writeByte(b);
            }
            file.setLength(fileLength + len);
        }

        logger.debug("Запись новых данных");
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
