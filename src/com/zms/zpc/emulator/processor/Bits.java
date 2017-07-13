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
    public final BitControl pf; //奇偶

    public Bits(Regs regs) {
        this.regs = regs;
        pe = new BitControl("pe", regs, regs.cr0.getIndex(), 0);
        int index = regs.rflags.getIndex();
        of = new BitControl("of", regs, index, 11);
        af = new BitControl("af", regs, index, 4);
        cf = new BitControl("cf", regs, index, 0);
        zf = new BitControl("zf", regs, index, 6);
        pf = new BitControl("pf", regs, index, 2);
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

    private static final boolean[] parityMap;

    static {
        parityMap = new boolean[256];
        for (int i = 0; i < parityMap.length; i++)
            parityMap[i] = ((Integer.bitCount(i) & 0x1) == 0);
    }

    public boolean pf() {
        if ((status & PF) == 0) {
            return pf.get();
        } else {
            return parityMap[(int) (result) & 0xff];
        }
    }

    public boolean cf() {
        return false;//todo
    }

}
