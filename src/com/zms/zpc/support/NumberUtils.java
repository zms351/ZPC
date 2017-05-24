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

}
