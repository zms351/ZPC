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

    int BITS_BASE = 200;
    int ROL = BITS_BASE;
    int ROR = BITS_BASE + 1;
    int RCL = BITS_BASE + 2;
    int RCR = BITS_BASE + 3;
    int SHL = BITS_BASE + 4;
    int SHR = BITS_BASE + 5;
    int SAL = BITS_BASE + 6;
    int SAR = BITS_BASE + 7;

    int ADD = 300;
    int OR = 301;
    int ADC = 302;
    int SBB = 303;
    int AND = 304;
    int SUB = 305;
    int XOR = 306;
    int CMP = 307;
    int TEST = 308;

    int CAL2_BASE = 400;
    int NOT = CAL2_BASE + 2;
    int NEG = CAL2_BASE + 3;
    int MUL = CAL2_BASE + 4;
    int IMUL = CAL2_BASE + 5;
    int DIV = CAL2_BASE + 6;
    int IDIV = CAL2_BASE + 7;

    int Call=501;
    int Ret=502;

    int LOG=30;
    int DEBUG = 31;
    int INFO = 32;
    int WARN = 33;

    int SREG=321;
    int CREG=322;
    int DREG=323;
    int TREG=324;

}
