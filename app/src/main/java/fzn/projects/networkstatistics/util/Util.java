package fzn.projects.networkstatistics.util;

/**
 * Created by FzN on 15/6/6.
 * 实用方法类
 */
public class Util {
    /**
     * 将字节转换成较大单位
     * @param bytes 字节数
     * @param isSpeed 是否为速度单位
     * @return 转换后的字符串
     */
    public static String byteConverter(long bytes, boolean isSpeed) {
        if (bytes > 0x40000000)
            return bytes / 0x40000000 + (isSpeed ? "GB/s" : "GB");
        else if (bytes > 0x100000)
            return bytes / 0x100000 + (isSpeed ? "MB/s" : "MB");
        else if (bytes > 0x400)
            return bytes / 0x400 + (isSpeed ? "KB/s" : "KB");
        else
            return bytes + (isSpeed ? "B/s" : "B");
    }
    public static int[] byteConverter(long bytes) {
        if (bytes > 0x40000000)
            return new int[] {(int)(bytes / 0x40000000), 3};
        else if (bytes > 0x100000)
            return new int[] {(int)(bytes / 0x100000), 2};
        else if (bytes > 0x400)
            return new int[] {(int)(bytes / 0x400), 1};
        else
            return new int[] {(int) bytes, 0};
    }
}
