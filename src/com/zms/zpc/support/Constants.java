package com.zms.zpc.support;

/**
 * Created by 张小美 on 17/七月/1.
 * Copyright 2002-2016
 */
public interface Constants {

    int CF = 1 /*<< 0*/;
    int PF = 1 << 2;
    int AF = 1 << 4;
    int ZF = 1 << 6;
    int SF = 1 << 7;
    int OF = 1 << 11;
    int OSZAPC = CF | PF | AF | ZF | SF | OF;
    int OSZPC = CF | PF | ZF | SF | OF;
    int OSZP = PF | ZF | SF | OF;
    int SZAPC = CF | PF | AF | ZF | SF;
    int SZAP = SF | ZF | AF | PF;
    int SZP = SF | ZF | PF;
    int SP = SF | PF;
    int NCF = PF | AF | ZF | SF | OF;
    int NOFCF = PF | AF | ZF | SF;
    int NAFCF = PF | ZF | SF | OF;
    int NZ = CF | PF | AF | SF | OF;
    int NP = CF | ZF | AF | SF | OF;

}
