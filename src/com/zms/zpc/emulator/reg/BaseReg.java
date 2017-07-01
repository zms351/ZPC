package com.zms.zpc.emulator.reg;

import com.zms.zpc.emulator.processor.Regs;
import com.zms.zpc.support.NotImplException;

/**
 * Created by 张小美 on 17/六月/24.
 * Copyright 2002-2016
 */
public abstract class BaseReg {

    public static final long Mask8l = 0xffffffffffffff00L;
    public static final int Mask8r = 0xff;

    public static final long Mask16l = 0xffffffffffff0000L;
    public static final int Mask16r = 0xffff;

    public static final long Mask32l = 0xffffffff00000000L;
    public static final int Mask32r = 0xffffffff;

    protected String name;
    protected long[] rvs;
    protected int index;
    protected int pos;
    protected int width;
    protected Regs regs;

    public BaseReg(String name, Regs regs, int index, int pos, int width) {
        this.name = name;
        this.regs = regs;
        this.rvs = regs.getRvs();
        this.index = index;
        this.pos = pos;
        this.width = width;
    }

    public String getName() {
        return name;
    }

    public long[] getRvs() {
        return rvs;
    }

    public int getIndex() {
        return index;
    }

    public int getPos() {
        return pos;
    }

    public int getWidth() {
        return width;
    }

    public Regs getRegs() {
        return regs;
    }

    public long getRv() {
        return rvs[index];
    }

    public void setRv(long v) {
        rvs[index] = v;
    }

    public abstract void setValue64(long v);

    public abstract void setValue32(int v);

    public abstract void setValue16(int v);

    public abstract void setValue8(int v);

    public abstract long getValue32();

    public long getValue64() {
        assert width == 64;
        return getRv();
    }

    public abstract int getValue16();

    public abstract int getValue8();

    public long getValue(int width) {
        switch (width) {
            case 8:
                return getValue8();
            case 16:
                return getValue16();
            case 32:
                return getValue32();
            case 64:
                return getValue64();
            default:
                throw new NotImplException();
        }
    }

    public int setValue(int width, long val) {
        switch (width) {
            case 8:
                setValue8((int) val);
                break;
            case 16:
                setValue16((int) val);
                break;
            case 32:
                setValue32((int) val);
                break;
            case 64:
                setValue64(val);
                break;
            default:
                throw new NotImplException();
        }
        return this.width;
    }

}
