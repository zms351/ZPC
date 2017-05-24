package com.zms.zpc.emulator.processor;

/**
 * Created by 张小美 on 17/五月/24.
 * Copyright 2002-2016
 */
public class Reg {

    private String name;
    private long[] rvs;
    private int index;
    private int pos;
    private int width;
    private Regs regs;

    public Reg(String name, Regs regs,int index,int pos,int width) {
        this.name = name;
        this.regs=regs;
        this.rvs=regs.getRvs();
        this.index=index;
        this.pos=pos;
        this.width=width;
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

}
