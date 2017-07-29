package com.zms.zpc.emulator.reg;

import com.zms.zpc.emulator.processor.Regs;

/**
 * Created by 张小美 on 17/七月/29.
 * Copyright 2002-2016
 */
public class CS extends Segment {

    public CS(String name, Regs regs, int index) {
        super(name, regs, index);
    }

    @Override
    public void setValue16(int v, boolean changeBase) {
        super.setValue16(v, changeBase);
    }

}
