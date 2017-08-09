package com.zms.zpc.emulator.memory;

import java.util.Arrays;

/**
 * Created by 张小美 on 17/八月/9.
 * Copyright 2002-2016
 */
public class MappedMemory extends PhysicalMemory {

    public RAM[] maps;

    public synchronized void install(RAM ram, long offset, long len) {
        long k = 4096;
        long a = offset / k;
        long b = (offset + len) / k;
        assert a < Integer.MAX_VALUE && b < Integer.MAX_VALUE;
        assert a * k == offset && b * k == (offset + len);
        int b_ = (int) b;
        if (maps == null) {
            maps = new RAM[b_];
        } else if (maps.length < b_) {
            maps = Arrays.copyOf(maps, b_);
        }
        for (int i = (int) a; i < b_; i++) {
            maps[i] = ram;
        }
    }

    public RAM getRam(long offset) {
        if (maps == null) {
            return null;
        }
        long n = offset >> 12;
        if (n > 0 && n < maps.length) {
            return maps[(int) n];
        }
        return null;
    }

    public MappedMemory(long size, int count) {
        super(size, count);
    }

    @Override
    public int read(long context, long pos) {
        RAM ram = getRam(pos);
        if (ram != null) {
            return ram.read(context, pos);
        } else {
            return super.read(context, pos);
        }
    }

    @Override
    public void write(long context, long pos, int v) {
        RAM ram = getRam(pos);
        if (ram != null) {
            ram.write(context, pos, v);
        } else {
            super.write(context, pos, v);
        }
    }

}
