package com.zms.zpc.emulator.processor;

import com.zms.zpc.emulator.processor.reg.BitControl;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public class Bits {

    public Regs regs;

    public final BitControl pe;

    public Bits(Regs regs) {
        this.regs = regs;
        pe = new BitControl("pe", regs, regs.cr0.getIndex(), 0);
    }

}
