package com.example.hexeditor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class DataInterpreter {

    public static short toShort(byte[][] data, boolean signed) {
        ByteBuffer buffer = ByteBuffer.wrap(flatten2DByteArray(data)).order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getShort(); // для unsigned нужно кастить
    }

    public static int toInt(byte[][] data, boolean signed) {
        ByteBuffer buffer = ByteBuffer.wrap(flatten2DByteArray(data)).order(ByteOrder.LITTLE_ENDIAN);
        System.out.println(buffer);
        return buffer.getInt();
    }

    public static long toUnsignedInt(byte[][] data) {
        ByteBuffer buffer = ByteBuffer.wrap(flatten2DByteArray(data)).order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getInt() & 0xFFFFFFFFL;
    }

    public static long toLong(byte[][] data) {
        ByteBuffer buffer = ByteBuffer.wrap(flatten2DByteArray(data)).order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getLong();
    }

    public static float toFloat(byte[][] data) {
        ByteBuffer buffer = ByteBuffer.wrap(flatten2DByteArray(data)).order(ByteOrder.LITTLE_ENDIAN);
        System.out.println(buffer);
        return buffer.getFloat();
    }

    public static double toDouble(byte[][] data) {
        ByteBuffer buffer = ByteBuffer.wrap(flatten2DByteArray(data))
                .order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getDouble();
    }

    public static byte[] flatten2DByteArray(byte[][] arr) {
        int totalLength = 0;
        for (byte[] subArray : arr) {
            totalLength += subArray.length;
        }

        byte[] result = new byte[totalLength];
        int position = 0;

        for (byte[] subArray : arr) {
            System.arraycopy(subArray, 0, result, position, subArray.length);
            position += subArray.length;
        }
        System.out.println(Arrays.toString(result));
        return result;
    }
}
