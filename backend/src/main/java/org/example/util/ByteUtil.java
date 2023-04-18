package org.example.util;

public class ByteUtil {
    public static int getMsb(int n) {
        return (n & 0xFFFFFFF0);
    }

    public static int getLsb(int n) {
        return (n & 0x0000000F);
    }

}
