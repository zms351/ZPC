package com.zms.zpc.emulator.reg;

import com.zms.zpc.emulator.processor.Regs;

/**
 * Created by 张小美 on 17/七月/27.
 * Copyright 2002-2016
 */
public class BigSegment extends Segment {

    public BigSegment(String name, Regs regs, int index) {
        super(name, regs, index, 64);
    }

    @Override
    public void setValue64(long v) {
        super.setValue64(v);
        base=v;
    }

}
