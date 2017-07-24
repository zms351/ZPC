package com.zms.zpc.emulator.processor;

import com.zms.zpc.emulator.reg.BitControl;
import com.zms.zpc.support.*;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public class Bits extends BaseObj {

    public Regs regs;

    public final BitControl pe; //protect mode

    public final BitControl of; //over flow
    public final BitControl af; //BCD相关进位
    public final BitControl cf; //进位
    public final BitControl zf; //进位
    public final BitControl pf; //奇偶
    public final BitControl if_; //中断
    public final BitControl df; //direction
    public final BitControl tf; //trap
    public final BitControl ac; //AlignmentCheck
    public final BitControl rf; //resume

    public Bits(Regs regs) {
        this.regs = regs;
        pe = new BitControl("pe", regs, regs.cr0.getIndex(), 0);
        int index = regs.rflags.getIndex();
        of = new BitControl("of", regs, index, 11);
        af = new BitControl("af", regs, index, 4);
        cf = new BitControl("cf", regs, index, 0);
        zf = new BitControl("zf", regs, index, 6);
        pf = new BitControl("pf", regs, index, 2);
        if_ = new BitControl("if", regs, index, 9);
        df = new BitControl("df", regs, index, 10);
        tf = new BitControl("tf", regs, index, 8);
        ac = new BitControl("ac", regs, index, 18);
        rf = new BitControl("rf", regs, index, 16);
    }

    private long result, op1, op2;
    private int status, ins, opWidth;

    public void setData(long op1, long op2, long result, int ins, int opWidth, int status) {
        this.op1 = op1;
        this.op2 = op2;
        this.result = result;
        this.ins = ins;
        this.opWidth = opWidth;
        this.status = status;
    }

    public long getResult() {
        return result;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getOp1() {
        return op1;
    }

    public long getOp2() {
        return op2;
    }

    public int getIns() {
        return ins;
    }

    public void clearOCA() {
        of.clear();
        cf.clear();
        af.clear();
    }

    public void clearITACR() {
        if_.clear();
        tf.clear();
        ac.clear();
        rf.clear();
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
        if ((status & CF) == 0) {
            return cf.get();
        } else {
            return testCF();
        }
    }

    public boolean of() {
        throw new NotImplException();
    }

    private boolean testCF() {
        switch (ins) {
            case ADD:
            case CMP:
            case SUB:
                switch (opWidth) {
                    case 8:
                        return (result & 0xff) != result;
                    case 16:
                        return (result & 0xffff) != result;
                    case 32:
                        return (result & 0xffffffffL) != result;
                    case 64:
                }
        }
        throw new NotImplException();
    }

    public CPUMode getMode() {
        return CPUMode.Real;
    }

}
