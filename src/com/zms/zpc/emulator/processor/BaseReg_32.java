package com.zms.zpc.emulator.processor;

import com.zms.zpc.base.NotImplException;

/**
 * Created by 张小美 on 17/六月/24.
 * Copyright 2002-2016
 */
public class BaseReg_32 extends BaseReg {

    public BaseReg_32(String name, Regs regs, int index) {
        super(name, regs, index, 32, 32);
    }

    @Override
    public void setValue32(int v) {

    }

    @Override
    public void setValue16(int v) {
        throw new NotImplException();
    }

}
