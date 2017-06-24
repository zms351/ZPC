package com.zms.zpc.emulator.processor;

import com.zms.zpc.base.NotImplException;

/**
 * Created by 张小美 on 17/六月/24.
 * Copyright 2002-2016
 */
public class BaseReg_8 extends BaseReg {

    public static final long Mask1 = 0xffffffffffff00ffL;
    public static final int Mask2 = 0xff00;

    public BaseReg_8(String name, Regs regs, int index) {
        super(name, regs, index, 8, 8);
    }

    @Override
    public void setValue32(int v) {
        throw new NotImplException();
    }

    @Override
    public void setValue16(int v) {
        throw new NotImplException();
    }

    @Override
    public void setValue8(int v) {
        assert width == 8;
        long n = getRv();
        n = (n & Mask1) | ((v << 8) & Mask2);
        setRv(n);
    }

}
