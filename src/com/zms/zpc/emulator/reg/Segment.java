package com.zms.zpc.emulator.reg;

import com.zms.zpc.emulator.processor.Regs;
import com.zms.zpc.support.NotImplException;

/**
 * Created by 张小美 on 17/六月/22.
 * Copyright 2002-2016
 */
public class Segment extends Reg {

    public BaseReg_16 attribute;
    public BaseReg_32 limit;
    public long base;

    public Segment(String name, Regs regs, int index) {
        super(name, regs, index, 16);
        attribute = new BaseReg_16(name + "r", regs, index);
        limit = new BaseReg_32(name + "l", regs, index);
    }

    @Override
    public void setValue16(int v) {
        this.setValue16(v, true);
    }

    public void setValue16(int v, boolean changeBase) {
        super.setValue16(v);
        if (changeBase) {
            switch (regs.cpu.getMode()) {
                case Real:
                    base = (v & 0xffff) << 4;
                    break;
                default:
                    throw new NotImplException();
            }
        }
    }

    public long getAddress(long address) {
        switch (regs.cpu.getMode()) {
            case Protected32:
            case Real:
                return base + (address & 0xffff);
            default:
                throw new NotImplException();
        }
    }

    public long getAddress(BaseReg pointer) {
        return getAddress(pointer.getValue());
    }

}
