package com.zms.zpc.emulator.processor.reg;

import com.zms.zpc.base.NotImplException;
import com.zms.zpc.emulator.processor.Regs;

/**
 * Created by 张小美 on 17/六月/24.
 * Copyright 2002-2016
 */
public class BaseReg_32 extends BaseReg {

    public static final long Mask = 0xffffffffL;

    public BaseReg_32(String name, Regs regs, int index) {
        super(name, regs, index, 32, 32);
    }

    @Override
    public void setValue32(int v) {
        assert width == 32;
        long n = getRv();
        n = (n & Mask) | (((long) v) << 32);
        setRv(n);
    }

    @Override
    public void setValue16(int v) {
        throw new NotImplException();
    }

    @Override
    public void setValue8(int v) {
        throw new NotImplException();
    }

}
