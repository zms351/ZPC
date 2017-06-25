package com.zms.zpc.emulator.processor.reg;

import com.zms.zpc.emulator.processor.Regs;

/**
 * Created by 张小美 on 17/六月/24.
 * Copyright 2002-2016
 */
public class SegmentBase extends BaseReg_32 {

    private int limit;
    private int ar;

    public SegmentBase(String name, Regs regs, int index) {
        super(name, regs, index);
    }

}
