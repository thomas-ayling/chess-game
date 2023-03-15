package org.example.util;

public class ByteUtil {
    public static int getMsb(int n) {
        return (n & 0xFFFFFFF0);
    }

    public static int getLsb(int n) {
        return (n & 0x0000000F);
    }

    public static void printBin(long n) {
        System.out.println(String.format("%64s", Long.toBinaryString(n)).replace(' ', '0'));
    }
}
