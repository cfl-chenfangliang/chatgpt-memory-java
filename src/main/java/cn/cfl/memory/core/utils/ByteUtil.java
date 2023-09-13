package cn.cfl.memory.core.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author chen.fangliang
 */
public class ByteUtil {

    public static byte[] getBytes(String str) {
        if (str == null) {
            str = "";
        }
        return str.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] floatArrayToByteArray(List<Float> floats) {
        return floatArrayToByteArray(floats.toArray(new Float[0]));
    }

    /**
     * float数组转byte数组
     */
    public static byte[] floatArrayToByteArray(Float[] floatArray) {
        ByteBuffer buffer = ByteBuffer.allocate(floatArray.length * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (float value : floatArray) {
            buffer.putFloat(value);
        }
        return buffer.array();
    }
}
