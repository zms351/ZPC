package com.zms.zpc.emulator.hardware;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public class PhysicalMemory extends RAM {

    private long totalSize;

    public PhysicalMemory(long size, int count) {
        this.totalSize = size * count;
    }

    public long getTotalSize() {
        return totalSize;
    }

}
