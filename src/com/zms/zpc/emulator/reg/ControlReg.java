package com.zms.zpc.emulator.reg;

import com.zms.zpc.emulator.processor.Regs;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public class ControlReg extends Reg {

    public ControlReg(String name, Regs regs, int index, int width) {
        super(name, regs, index, width);
    }

    @Override
    public long getValue32() {
        return super.getValue64();
    }

    @Override
    public void setValue32(int v) {
        setValue64(v);
    }

    @Override
    public void setValue64(long v) {
        super.setValue64(v);
        regs.cpu.checkState();
    }

}
