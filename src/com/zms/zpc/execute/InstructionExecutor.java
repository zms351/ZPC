package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.support.NotImplException;

/**
 * Created by 张小美 on 17/六月/27.
 * Copyright 2002-2016
 */
public class InstructionExecutor extends Instruction {

    public long read64(CodeInputStream input) {
        return read32(input) | read32(input) << 32;
    }

    public long read32(CodeInputStream input) {
        int a = read16(input);
        long b = read16(input);
        return b << 16 | a;
    }

    public int read16(CodeInputStream input) {
        return input.read() | (input.read() << 8);
    }

    public long readOp(CodeExecutor executor, CodeInputStream input) {
        int bits = executor.getBits();
        if (bits == 8) {
            return input.read();
        }
        if (bits == 16) {
            if (isHas66()) {
                return read32(input);
            } else {
                return read16(input);
            }
        }
        if (bits == 32) {
            if (isHas66()) {
                return read16(input);
            } else {
                return read32(input);
            }
        }
        if (bits == 64) {
            if (isHasRexW()) {
                return read64(input);
            } else if (isHas66()) {
                return read16(input);
            } else {
                return read32(input);
            }
        }
        throw new NotImplException();
    }

    public void executeJumpFar(CodeExecutor executor, CodeInputStream input, PC pc) {
        long offset = readOp(executor, input);
        int base = read16(input);
        pc.cpu.regs.cs.setValue16(base, true);
        pc.cpu.regs.eip.setValue32((int) offset);
    }

    public void executeXor31(CodeExecutor executor, CodeInputStream input, PC pc) {
    }

}
