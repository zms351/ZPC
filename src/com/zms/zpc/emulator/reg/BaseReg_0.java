package com.zms.zpc.emulator.reg;

import com.zms.zpc.emulator.processor.Regs;

/**
 * Created by 张小美 on 17/六月/24.
 * Copyright 2002-2016
 */
public class BaseReg_0 extends BaseReg {

    public BaseReg_0(String name, Regs regs, int index, int width) {
        super(name, regs, index, 0, width);
    }

    @Override
    public void setValue32(int v) {
        assert width == 32;
        long n = getRv();
        n = (n & Mask32l) | v;
        setRv(n);
    }

    @Override
    public void setValue16(int v) {
        assert width == 16;
        long n = getRv();
        n = (n & Mask16l) | (v & Mask16r);
        setRv(n);
    }

    @Override
    public void setValue8(int v) {
        assert width == 8;
        long n = getRv();
        n = (n & Mask8l) | (v & Mask8r);
        setRv(n);
    }

    @Override
    public int getValue32() {
        assert width==32;
        return (int) getRv();
    }

    @Override
    public void setValue64(long v) {
        assert width==64;
        setRv(v);
    }

}
