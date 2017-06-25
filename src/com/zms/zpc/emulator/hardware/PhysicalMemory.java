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

    @Override
    public void write(long context, long pos, int v) {
        memory[(int)pos]= (byte) v;
    }

    @Override
    public int read(long context, long pos, byte[] bytes, int offset, int size) {
        System.arraycopy(memory, (int) pos,bytes,offset,size);
        return size;
    }

    @Override
    public int write(long context, long pos, byte[] bytes, int offset, int size) {
        System.arraycopy(bytes,offset,memory, (int) pos,size);
        return size;
    }

}
