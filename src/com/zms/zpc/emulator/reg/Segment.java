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
    public DTR dtr;

    public Segment(String name, Regs regs, int index) {
        super(name, regs, index, 16);
        attribute = new BaseReg_16(name + "r", regs, index);
        limit = new BaseReg_32(name + "l", regs, index);
        dtr = new DTR(this);
    }

    @Override
    public void setValue16(int v) {
        this.setValue16(v, true);
    }

    public void loadProtected() {
        long v = getValue();
        BigSegment big;
        long v1 = v >> 3;
        if ((v & 4) > 0) {
            big = regs.ldtr;
        } else {
            big = regs.gdtr;
            dtr._null=v1==0;
        }
        dtr.load(big.getValue(), v1);
    }

    public void setValue16(int v, boolean changeBase) {
        super.setValue16(v);
        if (changeBase) {
            switch (regs.cpu.getMode()) {
                case Real:
                    base = (v & 0xffff) << 4;
                    break;
                case Protected16:
                case Protected32:
                    loadProtected();
                    regs.cpu.checkState();
                    break;
                default:
                    throw new NotImplException();
            }
        }
    }

    public long getAddress(long address) {
        switch (regs.cpu.getMode()) {
            case Protected16:
            case Real:
                return base + (address & 0xffff);
            case Protected32:
                return base+(address & 0xffffffffL);
            default:
                throw new NotImplException();
        }
    }

    public long getAddress(BaseReg pointer) {
        return getAddress(pointer.getValue());
    }

    @Override
    public void setValue32(int v) {
        setValue16(v);
    }

}
