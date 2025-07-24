package com.example.hexeditor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Утилитарный класс для преобразования двумерных массивов байт
 * в примитивные типы данных с little-endian порядком байт.
 * Поддерживает преобразование в следующие типы:
 *   Целочисленные: short, int, long
 *   Числа с плавающей точкой: float, double
 *   Беззнаковые целые (unsigned int)
 * Нулевые массивы и подмассивы обрабатываются корректно (возвращаются как пустые данные).
 */
public class DataInterpreter {

    /**
     * Преобразует двумерный массив байт в значение типа short.
     *
     * @param data двумерный массив байт (может быть null или содержать null-подмассивы)
     * @param signed если true - интерпретировать как знаковое число
     * @return преобразованное значение short
     * @see ByteBuffer#getShort()
     */
    public static short toShort(byte[][] data, boolean signed) {
        return ByteBuffer.wrap(flatten(data))
                .order(ByteOrder.LITTLE_ENDIAN)
                .getShort();
    }

    /**
     * Преобразует двумерный массив байт в значение типа int.
     *
     * @param data двумерный массив байт (может быть null или содержать null-подмассивы)
     * @param signed если true - интерпретировать как знаковое число
     * @return преобразованное значение int
     * @see ByteBuffer#getInt()
     */
    public static int toInt(byte[][] data, boolean signed) {
        return ByteBuffer.wrap(flatten(data))
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
    }

    /**
     * Преобразует двумерный массив байт в беззнаковое значение типа long (32 бита).
     *
     * @param data двумерный массив байт (может быть null или содержать null-подмассивы)
     * @return преобразованное беззнаковое значение в диапазоне long
     */
    public static long toUnsignedInt(byte[][] data) {
        return toInt(data, false) & 0xFFFFFFFFL;
    }

    /**
     * Преобразует двумерный массив байт в значение типа long.
     *
     * @param data двумерный массив байт (может быть null или содержать null-подмассивы)
     * @return преобразованное значение long
     * @see ByteBuffer#getLong()
     */
    public static long toLong(byte[][] data) {
        return ByteBuffer.wrap(flatten(data))
                .order(ByteOrder.LITTLE_ENDIAN)
                .getLong();
    }

    /**
     * Преобразует двумерный массив байт в значение типа float.
     *
     * @param data двумерный массив байт (может быть null или содержать null-подмассивы)
     * @return преобразованное значение float
     * @see ByteBuffer#getFloat()
     */
    public static float toFloat(byte[][] data) {
        return ByteBuffer.wrap(flatten(data))
                .order(ByteOrder.LITTLE_ENDIAN)
                .getFloat();
    }

    /**
     * Преобразует двумерный массив байт в значение типа double.
     *
     * @param data двумерный массив байт (может быть null или содержать null-подмассивы)
     * @return преобразованное значение double
     * @see ByteBuffer#getDouble()
     */
    public static double toDouble(byte[][] data) {
        return ByteBuffer.wrap(flatten(data))
                .order(ByteOrder.LITTLE_ENDIAN)
                .getDouble();
    }

    /**
     * Преобразует двумерный массив байт в одномерный.
     * Обрабатывает null-значения:
     *   Если входной массив null - возвращает пустой массив
     *   Если подмассивы null - они пропускаются
     *
     * @param data двумерный массив байт (может быть null)
     * @return одномерный массив байт (никогда не null)
     */
    private static byte[] flatten(byte[][] data) {
        if (data == null) return new byte[0];

        int length = 0;
        for (byte[] arr : data) {
            length += arr != null ? arr.length : 0;
        }

        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] arr : data) {
            if (arr != null) {
                System.arraycopy(arr, 0, result, pos, arr.length);
                pos += arr.length;
            }
        }
        return result;
    }
}
