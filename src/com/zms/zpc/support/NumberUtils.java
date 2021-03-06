package com.zms.zpc.support;

/**
 * Created by 张小美 on 17/五月/25.
 * Copyright 2002-2016
 */
public class NumberUtils {

    public static String toHex(long n, int width) {
        String s = "00000000000000000000000000000000000000000000000000000000000000000000" + Long.toHexString(n);
        return s.substring(s.length() - width);
    }

    public static String toBin(long n, int width) {
        String s = "00000000000000000000000000000000000000000000000000000000000000000000" + Long.toBinaryString(n);
        return s.substring(s.length() - width);
    }

    public static String byte2Hex(int b) {
        String token = Integer.toHexString(b + 512);
        return token.substring(token.length() - 2);
    }

    public static boolean isNumber(String s) {
        if (s == null || (s = s.trim().toUpperCase()).length() < 1) {
            return false;
        }
        char c = s.charAt(0);
        if (c == '+' || c == '-') {
            return isNumber(s.substring(1));
        }
        if (s.matches("^[0-9A-F]+$")) {
            return true;
        }
        if (s.startsWith("0X")) {
            return isNumber(s.substring(2));
        }
        if (s.startsWith("0B")) {
            s = s.substring(2);
            if (s.matches("^[01]+$")) {
                return true;
            }
        }
        return false;
    }

    public static long parseNumber(String s) {
        s = s.trim().toUpperCase();
        assert s.length() > 0;
        char c = s.charAt(0);
        if (c == '+') {
            return parseNumber(s.substring(1));
        }
        if (c == '-') {
            return -parseNumber(s.substring(1));
        }
        if (s.startsWith("0X")) {
            return Long.parseLong(s.substring(2), 16);
        }
        if (s.startsWith("0B")) {
            return Long.parseLong(s.substring(2), 2);
        }
        if (s.startsWith("0")) {
            return Long.parseLong(s, 8);
        }
        if (s.matches("^\\d+$")) {
            return Long.parseLong(s, 10);
        } else {
            return Long.parseLong(s, 16);
        }
    }

    public static boolean isIn32Bits(long n) {
        return n >= Integer.MIN_VALUE && n < 4294967296L;
    }

    public static boolean isIn16Bits(long n) {
        return n >= Short.MIN_VALUE && n < 65536;
    }

    public static boolean isIn8Bits(long n) {
        return n >= Byte.MIN_VALUE && n < 256;
    }

    public static final long[] Powers = new long[64];

    static {
        long n = 1;
        for (int i = 0; i < 64; i++) {
            Powers[i] = n;
            n *= 2;
        }
    }

    public static long asSigned(long n, int width) {
        switch (width) {
            case 8:
                return (byte) n;
            case 16:
                return (short) n;
            case 32:
                return (int) n;
            case 64:
                return n;
            default:
                throw new NotImplException();
        }
    }

    public static long zeroExtend32_2_64(long n) {
        return zeroExtend(n, 32);
    }

    public static long signExtend32_2_64(long n) {
        return (int) n;
    }

    public static long signExtend8_2_64(long n) {
        return (byte) n;
    }

    public static long signExtend8_2_32(long n) {
        int v = (byte) n;
        return v & 0xffffffffL;
    }

    public static long signExtend16_2_32(long n) {
        short v = (short) n;
        return v & 0xffffffffL;
    }

    public static long signExtend8_2_16(long n) {
        int v = (byte) n;
        return v & 0xffffL;
    }

    public static long signExtend(long n, int from, int to) {
        if (from == 8 && to == 16) {
            return signExtend8_2_16(n);
        } else if (from == 16 && to == 32) {
            return signExtend16_2_32(n);
        } else if (from == 32 && to == 64) {
            return signExtend32_2_64(n);
        } else {
            throw new NotImplException();
        }
    }

    public static long signExtend8(long n, int width) {
        switch (width) {
            case 8:
                return n & 0xff;
            case 16:
                return signExtend8_2_16(n);
            case 32:
                return signExtend8_2_32(n);
            case 64:
                return signExtend8_2_64(n);
            default:
                throw new NotImplException();
        }
    }

    public static long zeroExtend(long n, int width) {
        switch (width) {
            case 8:
                return n & 0xffL;
            case 16:
                return n & 0xffffL;
            case 32:
                return n & 0xffffffffL;
            case 64:
                return n;
            default:
                throw new NotImplException();
        }
    }

    public static boolean hasSign(long n, int width) {
        switch (width) {
            case 8:
                return (n & 0x80) != 0;
            case 16:
                return (n & 0x8000) != 0;
            case 32:
                return (n & 0x80000000) != 0;
            case 64:
                return (n & 0x8000000000000000L) != 0;
            default:
                throw new NotImplException();
        }
    }

}
