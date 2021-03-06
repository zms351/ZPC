package com.zms.zpc.emulator.reg;

import com.zms.zpc.support.*;
import com.zms.zpc.emulator.processor.Regs;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public class BitControl extends BaseReg {

    private long mask;

    public BitControl(String name, Regs regs, int index, int pos) {
        super(name, regs, index, pos, 1);
        mask= NumberUtils.Powers[pos];
    }

    @Override
    public void setValue32(int v) {
        throw new NotImplException();
    }

    @Override
    public void setValue16(int v) {
        throw new NotImplException();
    }

    @Override
    public void setValue8(int v) {
        throw new NotImplException();
    }

    @Override
    public long getValue32() {
        throw new NotImplException();
    }

    @Override
    public void setValue64(long v) {
        throw new NotImplException();
    }

    public boolean get() {
        return (getRv() & mask)!=0;
    }

    public void set() {
        set(true);
    }

    public void set(boolean set) {
        long n=getRv();
        if(set) {
            n=n | mask;
        } else {
            n=n & (~mask);
        }
        setRv(n);
    }

    public void clear() {
        set(false);
    }

    @Override
    public int getValue16() {
        throw new NotImplException();
    }

    @Override
    public int getValue8() {
        throw new NotImplException();
    }

    public void not() {
        set(!get());
    }

}
