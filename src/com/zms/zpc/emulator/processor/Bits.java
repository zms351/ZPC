package com.zms.zpc.emulator.processor;

import com.zms.zpc.emulator.reg.BitControl;
import com.zms.zpc.support.Constants;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public class Bits implements Constants {

    public Regs regs;

    public final BitControl pe; //protect mode

    public final BitControl of; //over flow
    public final BitControl af; //BCD相关进位
    public final BitControl cf; //进位
    public final BitControl zf; //进位

    public Bits(Regs regs) {
        this.regs = regs;
        pe = new BitControl("pe", regs, regs.cr0.getIndex(), 0);
        of = new BitControl("of", regs, regs.rflags.getIndex(), 11);
        af = new BitControl("af", regs, regs.rflags.getIndex(), 4);
        cf = new BitControl("cf", regs, regs.rflags.getIndex(), 0);
        zf = new BitControl("zf", regs, regs.rflags.getIndex(), 6);
    }

    public long result, op1, op2;
    public int status, ins, opWidth;

    public void clearOCA() {
        of.clear();
        cf.clear();
        af.clear();
    }

    public boolean zf() {
        if ((status & ZF) == 0) {
            return zf.get();
        } else {
            return result == 0;
        }
    }

}
