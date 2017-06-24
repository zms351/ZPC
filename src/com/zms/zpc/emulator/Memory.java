package com.zms.zpc.emulator;

/**
 * Created by 张小美 on 17/六月/24.
 * Copyright 2002-2016
 */
public class Memory {

    private long totalSize;

    public Memory(long size, int count) {
        this.totalSize = size * count;
    }

    public long getTotalSize() {
        return totalSize;
    }

}
