package fzn.projects.networkstatistics.util;

import java.text.DecimalFormat;

/**
 * Created by FzN on 15/6/6.
 * 实用方法类
 */
public class Util {
    @android.support.annotation.NonNull
    private static DecimalFormat df = new DecimalFormat();
    /**
     * 将字节转换成较大单位
     * @param bytes 字节数
     * @param isSpeed 是否为速度单位
     * @return 转换后的字符串
     */
    @android.support.annotation.NonNull
    public static String byteConverter(long bytes, boolean isSpeed, @android.support.annotation.NonNull String format) {
        df.applyPattern(format);
        if (bytes > 0x40000000)
            return df.format((float) bytes / 0x40000000) + (isSpeed ? "GB/s" : "GB");
        else if (bytes > 0x100000)
            return df.format((float) bytes / 0x100000) + (isSpeed ? "MB/s" : "MB");
        else if (bytes > 0x400)
            return df.format((float) bytes / 0x400) + (isSpeed ? "KB/s" : "KB");
        else
            return bytes + (isSpeed ? "B/s" : "B");
    }

    @android.support.annotation.NonNull
    public static float[] byteConverter(long bytes) {
        if (bytes > 0x40000000)
            return new float[] {((float) bytes / 0x40000000), 3};
        else if (bytes > 0x100000)
            return new float[] {((float) bytes / 0x100000), 2};
        else if (bytes > 0x400)
            return new float[] {((float) bytes / 0x400), 1};
        else
            return new float[] {bytes, 0};
    }

    @android.support.annotation.NonNull
    public static String[] byteConverterUnitSplit(long bytes, boolean isSpeed, @android.support.annotation.NonNull String format) {
        df.applyPattern(format);
        if (bytes > 0x40000000)
            return new String[] {df.format((float) bytes / 0x40000000), (isSpeed ? "GB/s" : "GB")};
        else if (bytes > 0x100000)
            return new String[] {df.format((float) bytes / 0x100000), (isSpeed ? "MB/s" : "MB")};
        else if (bytes > 0x400)
            return new String[] {df.format((float) bytes / 0x400), (isSpeed ? "KB/s" : "KB")};
        else
            return new String[] {df.format(bytes), (isSpeed ? "B/s" : "B")};
    }

    public static short[] resolveComboPeriod(@android.support.annotation.NonNull String period) {
        switch (period.charAt(period.length() - 1)) {
            case 'm':
                return new short[] {Short.valueOf(period.substring(0, period.length() - 1)), 0};
            case 'd':
                return new short[] {Short.valueOf(period.substring(0, period.length() - 1)), 1};
            case 'w':
                return new short[] {Short.valueOf(period.substring(0, period.length() - 1)), 2};
            case 'y':
                return new short[] {Short.valueOf(period.substring(0, period.length() - 1)), 3};
            default:
                return null;
        }
    }

    /**
     * Split a string of time patterned of XX:XX[:XX]
     * @param time The string of time
     * @return an array of short integers
     */
    @android.support.annotation.NonNull
    public static byte[] splitTime(@android.support.annotation.NonNull String time) {
        String[] times = time.split(":");
        byte[] split = new byte[times.length];
        for (int i = 0; i < times.length; i++) {
            split[i] = Byte.valueOf(times[i]);
        }
        return split;
    }
}
