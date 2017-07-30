package com.zms.zpc.support;

/**
 * Created by 张小美 on 17/七月/23.
 * Copyright 2002-2016
 */
public class BaseObj implements Constants {

    public static int Debug=1;
    public static long[] Pows;

    static {
        Pows =new long[64];
        Pows[0]=1;
        for(int i = 1; i< Pows.length; i++) {
            Pows[i]= Pows[i-1]*2;
        }
    }

}
