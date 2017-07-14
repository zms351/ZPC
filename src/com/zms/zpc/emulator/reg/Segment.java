package com.zms.zpc.emulator.reg;

import com.zms.zpc.emulator.processor.Regs;

/**
 * Created by 张小美 on 17/六月/22.
 * Copyright 2002-2016
 */
public class Segment extends Reg {

    public BaseReg_16 attribute;
    public BaseReg_32 limit;
    public Reg base;

    public Segment(String name, Regs regs, int index) {
        super(name, regs, index, 16);
        attribute = new BaseReg_16(name + "r", regs, index);
        limit = new BaseReg_32(name + "l", regs, index);
        base = new Reg(name + "b", regs, index + 6, 64);
    }

    public void setValue16(int v, boolean changeBase) {
        super.setValue16(v);
        if (changeBase) {
            base.setValue64((v & 0xffff) << 4);
        }
    }

    public long getAddress(BaseReg pointer) {
        return base.getValue64() + pointer.getValue64();
    }

}
