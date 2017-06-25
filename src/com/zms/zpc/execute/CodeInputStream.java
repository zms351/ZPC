package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.hardware.RAM;
import com.zms.zpc.emulator.processor.Regs;
import com.zms.zpc.support.NotImplException;

import java.io.*;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public class CodeInputStream extends InputStream {

    private long pos;
    private RAM ram;

    public void seek(PC pc) {
        Regs regs = pc.cpu.regs;
        if (regs.bits.pe.get()) {
            throw new NotImplException();
        } else {
            ram = pc.memory;
            pos = (regs.cs.getBase() & 0xffffffffL) + (regs.eip.getValue32() & 0xffffffffL);
        }
    }

    @Override
    public int read() throws IOException {
        return ram.read(0,pos);
    }

}
