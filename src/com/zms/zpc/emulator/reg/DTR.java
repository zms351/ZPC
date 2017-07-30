package com.zms.zpc.emulator.reg;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.support.BaseObj;

/**
 * Created by 张小美 on 17/七月/30.
 * Copyright 2002-2016
 */
public class DTR extends BaseObj {

    public Segment segment;
    public boolean _null;

    public long val;

    public DTR(Segment segment) {
        this.segment=segment;
    }

    public void load(long address, long index) {
        val=PC.currentPC.get().memory.read(1,address+index*8,64);
        long limit=(val & 0xffff) | ((val & 0xf000000000000L)>>32);
        segment.base= ((val & 0xffffff0000L)>>16) | ((val >> 32) & 0xff000000L);
        segment.limit.setValue(limit);
    }

    public boolean hasG() {
        return (val & Pows[55])>0;
    }

    public boolean hasDB() {
        return (val & Pows[54])>0;
    }

    public boolean isNull() {
        return _null;
    }

}
