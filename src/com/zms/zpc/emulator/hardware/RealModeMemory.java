package com.zms.zpc.emulator.hardware;

import com.zms.zpc.support.NotImplException;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public class RealModeMemory extends RAM {

    private PhysicalMemory memory;

    public RealModeMemory(RAM ram) {
        if(ram instanceof PhysicalMemory) {
            memory= (PhysicalMemory) ram;
        } else if(ram instanceof RealModeMemory) {
            memory=((RealModeMemory)ram).memory;
        } else {
            throw new NotImplException();
        }
    }

}
