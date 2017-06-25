package com.zms.zpc.emulator.hardware;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public class PhysicalMemory extends RAM {

    private long totalSize;
    private byte[] memory;

    public PhysicalMemory(long size, int count) {
        this.totalSize = size * count;
        init();
    }

    public long getTotalSize() {
        return totalSize;
    }

    private void init() {
        assert totalSize>0 && totalSize<Integer.MAX_VALUE;
        memory=new byte[(int) totalSize];
    }

    public byte[] getMemory() {
        return memory;
    }

    @Override
    public int read(long context, long pos) {
        return memory[(int) pos];
    }

}
