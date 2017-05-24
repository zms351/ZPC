package com.zms.zpc.emulator.processor;

/**
 * Created by 张小美 on 17/五月/24.
 * Copyright 2002-2016
 */
public class Regs {

    private final long[] rvs = new long[128];

    private final Reg rax = new Reg("rax", this, 0, 0, 64);
    private final Reg eax = new Reg("eax", this, 0, 0, 32);
    private final Reg ax = new Reg("ax", this, 0, 0, 16);
    private final Reg ah = new Reg("ah", this, 0, 8, 8);
    private final Reg al = new Reg("al", this, 0, 0, 8);

    private final Reg rbx = new Reg("rbx", this, 1, 0, 64);
    private final Reg ebx = new Reg("ebx", this, 1, 0, 32);
    private final Reg bx = new Reg("bx", this, 1, 0, 16);
    private final Reg bh = new Reg("bh", this, 1, 8, 8);
    private final Reg bl = new Reg("bl", this, 1, 0, 8);

    private final Reg rcx = new Reg("rcx", this, 2, 0, 64);
    private final Reg ecx = new Reg("ecx", this, 2, 0, 32);
    private final Reg cx = new Reg("cx", this, 2, 0, 16);
    private final Reg ch = new Reg("ch", this, 2, 8, 8);
    private final Reg cl = new Reg("cl", this, 2, 0, 8);

    private final Reg rdx = new Reg("rdx", this, 3, 0, 64);
    private final Reg edx = new Reg("edx", this, 3, 0, 32);
    private final Reg dx = new Reg("dx", this, 3, 0, 16);
    private final Reg dh = new Reg("dh", this, 3, 8, 8);
    private final Reg dl = new Reg("dl", this, 3, 0, 8);

    public long[] getRvs() {
        return rvs;
    }

}
