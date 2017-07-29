package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.memory.RAM;
import com.zms.zpc.emulator.processor.Regs;

import java.io.InputStream;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public class CodeStream extends InputStream {

    private long pos;
    private RAM ram;

    public void seek(PC pc) {
        Regs regs = pc.cpu.regs;
        ram = pc.memory;
        pos = regs.cs.getAddress(regs.rip);
    }

    @Override
    public int read() {
        return ram.read(0, pos++) & 0xff;
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
        pos += total;
    }

    public int write(byte[] bytes) {
        return write(bytes, 0, bytes.length);
    }

    public int write(byte[] bytes, int offset, int len) {
        int n = ram.write(0, pos, bytes, offset, len);
        pos += n;
        return n;
    }

    public long getPos() {
        return pos;
    }

    public void setPos(long pos) {
        this.pos = pos;
    }

}
