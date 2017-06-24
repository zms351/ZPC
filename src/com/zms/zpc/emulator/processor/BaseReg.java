package com.zms.zpc.emulator.processor;

/**
 * Created by 张小美 on 17/六月/24.
 * Copyright 2002-2016
 */
public abstract class BaseReg {

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

    public abstract void setValue32(int v);

    public abstract void setValue16(int v);

}
