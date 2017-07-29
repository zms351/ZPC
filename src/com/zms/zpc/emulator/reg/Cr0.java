package com.zms.zpc.emulator.reg;

import com.zms.zpc.emulator.processor.*;

/**
 * Created by 张小美 on 17/七月/29.
 * Copyright 2002-2016
 */
public class Cr0 extends ControlReg {

    public Cr0(String name, Regs regs, int index, int width) {
        super(name, regs, index, width);
    }

    @Override
    public void setValue64(long v) {
        super.setValue64(v);
        if(!regs.bits.pe.get()) {
            regs.bits.setMode(CPUMode.Real);
        } else {
            if(regs.bits.getMode()==CPUMode.Real) {
                regs.bits.setMode(CPUMode.Protected32);
            }
        }
    }

}
