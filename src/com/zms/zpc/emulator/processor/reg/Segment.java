package com.zms.zpc.emulator.processor.reg;

import com.zms.zpc.emulator.processor.Regs;

import java.util.*;

/**
 * Created by 张小美 on 17/六月/22.
 * Copyright 2002-2016
 */
public class Segment extends Reg {

    public static final Map<Integer,SegmentBase> baseMap=new HashMap<>();

    private SegmentBase base;

    public Segment(String name, Regs regs, int index, int width) {
        super(name, regs, index, width);
        assert pos==0;
        synchronized (baseMap) {
            base = baseMap.get(index);
            if (base == null) {
                String baseName=name.substring(name.length()-2)+"b";
                base=new SegmentBase(baseName,regs,index);
                baseMap.put(index,base);
            }
        }
    }

    @Override
    public void setValue16(int v) {
        super.setValue16(v);
        setBase((v & 0xffff)<<4);
    }

    public void setBase(int v) {
        base.setValue32(v);
    }

    public int getBase() {
        return base.getValue32();
    }

}
