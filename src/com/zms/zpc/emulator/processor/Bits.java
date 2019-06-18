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
    public final BitControl sf; //sign
    public final BitControl if_; //中断
    public final BitControl df; //direction
    public final BitControl tf; //trap
    public final BitControl ac; //AlignmentCheck
    public final BitControl rf; //resume

    public Bits(Regs regs) {
        this.regs = regs;
        pe = new BitControl("pe", regs, regs.cr0.getIndex(), 0);
        int index = regs.rflags.getIndex();
        cf = new BitControl("cf", regs, index, 0);
        pf = new BitControl("pf", regs, index, 2);
        af = new BitControl("af", regs, index, 4);
        zf = new BitControl("zf", regs, index, 6);
        sf = new BitControl("sf", regs, index, 7);
        tf = new BitControl("tf", regs, index, 8);
        if_ = new BitControl("if", regs, index, 9);
        df = new BitControl("df", regs, index, 10);
        of = new BitControl("of", regs, index, 11);
        rf = new BitControl("rf", regs, index, 16);
        ac = new BitControl("ac", regs, index, 18);
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
        if ((status & OF) == 0) {
            return of.get();
        } else {
            switch (ins) {
                case ADD:
                    switch (opWidth) {
                        case 8:
                            return (((~((op1) ^ (op2)) & ((op2) ^ (result))) & (0x80)) != 0);
                        case 16:
                            return (((~((op1) ^ (op2)) & ((op2) ^ (result))) & (0x8000)) != 0);
                        case 32:
                            return (((~((op1) ^ (op2)) & ((op2) ^ (result))) & (0x80000000)) != 0);
                    }
                    break;
                case SUB:
                    switch (opWidth) {
                        case 8:
                            return (((((op1) ^ (op2)) & ((op1) ^ (result))) & (0x80)) != 0);
                        case 16:
                            return (((((op1) ^ (op2)) & ((op1) ^ (result))) & (0x8000)) != 0);
                        case 32:
                            return (((((op1) ^ (op2)) & ((op1) ^ (result))) & (0x80000000)) != 0);
                    }
                    break;
                case SHL:
                case SAL:
                    switch (opWidth) {
                        case 8:
                        case 16:
                        case 32:
                            return ((result >> (opWidth - 1)) != 0) ^ (((op1 >> (opWidth - op2)) & 0x1) != 0);
                    }
                    break;
                case SHR:
                    switch (opWidth) {
                        case 8:
                        case 16:
                        case 32:
                            return (((result << 1) ^ result) >> (opWidth - 1)) != 0;
                    }
                    break;
                case SAR:
                    return false;
            }
            throw new NotImplException();
        }
    }

    public boolean sf() {
        if ((status & SF) == 0) {
            return sf.get();
        } else {
            switch (opWidth) {
                case 8:
                    return ((byte) result) < 0;
                case 16:
                    return ((short) result) < 0;
                case 32:
                    return ((int) result) < 0;
                case 64:
                    return result < 0;
                default:
                    throw new NotImplException();
            }
        }
    }

    private boolean testCF() {
        if (opWidth == 64) {
            throw new NotImplException();
        }
        switch (ins) {
            case ADD:
            case CMP:
            case SUB:
                return (result & (Pows[opWidth] - 1)) != result;
            case SHL:
                if (op2 <= 0) {
                    return op2 < -100;
                }
                return ((op1 >> (opWidth - op2)) & 0x1) != 0;
            case SHR:
                if (op2 <= 0) {
                    return op2 < -100;
                }
                return ((op1 >> (op2 - 1)) & 0x1) != 0;
            case ADC: {
                long m = Pows[opWidth] - 1;
                long t1 = op1 & m;
                long t2 = op2 & m;
                if ((result & m) != t1 + t1) {
                    return t1 + t2 + 1 > m;
                } else {
                    return t1 + t2 > m;
                }
            }
        }
        throw new NotImplException();
    }

    public boolean af() {
        throw new NotImplException();
    }

}
