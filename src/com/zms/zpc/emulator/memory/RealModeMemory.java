package com.zms.zpc.emulator.memory;

import com.zms.zpc.support.NotImplException;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public class RealModeMemory extends RAM {

    private PhysicalMemory memory;

    public RealModeMemory(RAM ram) {
        if (ram instanceof PhysicalMemory) {
            memory = (PhysicalMemory) ram;
        } else if (ram instanceof RealModeMemory) {
            memory = ((RealModeMemory) ram).memory;
        } else {
            throw new NotImplException();
        }
    }

    @Override
    public int read(long context, long pos) {
        return memory.read(context, pos);
    }

    @Override
    public void write(long context, long pos, int v) {
        memory.write(context, pos, v);
    }

    @Override
    public int read(long context, long pos, byte[] bytes, int offset, int size) {
        return memory.read(context,pos,bytes,offset,size);
    }

    @Override
    public int write(long context, long pos, byte[] bytes, int offset, int size) {
        return memory.write(context,pos,bytes,offset,size);
    }

}
