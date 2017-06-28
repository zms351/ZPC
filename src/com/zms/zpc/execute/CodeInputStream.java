package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.hardware.RAM;
import com.zms.zpc.emulator.processor.Regs;
import com.zms.zpc.support.NotImplException;

import java.io.InputStream;

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
            pos = regs.cs.base.getValue64() + (regs.eip.getValue32() & 0xffffffffL);
        }
    }

    @Override
    public int read() {
        return ram.read(0, pos++);
    }

    @Override
    public int read(byte[] b, int off, int len) {
        int n = ram.read(0, pos, b, off, len);
        if (n > 0) {
            pos += n;
        }
        return n;
    }

    public void readFully(byte[] b) {
        readFully(b, 0, b.length);
    }

    public void readFully(byte[] b, int off, int len) {
        int total = 0;
        int n;
        while (total < len) {
            n = read(b, off + total, len - total);
            if (n > 0) {
                total += n;
            }
        }
    }

    public long getPos() {
        return pos;
    }

    public void setPos(long pos) {
        this.pos = pos;
    }

}
