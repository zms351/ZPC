package com.zms.zpc.emulator.reg;

import com.zms.zpc.emulator.processor.Regs;
import com.zms.zpc.support.NotImplException;

/**
 * Created by 张小美 on 17/六月/28.
 * Copyright 2002-2016
 */
public class BaseReg_16 extends BaseReg {

    public static final long Mask1 = 0xffffffff0000ffffL;
    public static final int Mask2 = 0xffff0000;

    public BaseReg_16(String name, Regs regs, int index) {
        super(name, regs, index, 16, 16);
    }

    @Override
    public void setValue64(long v) {
        throw new NotImplException();
    }

    @Override
    public void setValue32(int v) {
        throw new NotImplException();
    }

    @Override
    public void setValue16(int v) {
        assert width==16;
        long n = getRv();
        n = (n & Mask1) | ((v << 16) & Mask2);
        setRv(n);
    }

    @Override
    public void setValue8(int v) {
        throw new NotImplException();
    }

    @Override
    public long getValue32() {
        throw new NotImplException();
    }

    @Override
    public int getValue16() {
        assert width==16;
        return (int) ((getRv() & Mask2)>>>16);
    }

    @Override
    public int getValue8() {
        throw new NotImplException();
    }

}
