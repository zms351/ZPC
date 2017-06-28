package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;

/**
 * Created by 张小美 on 17/六月/27.
 * Copyright 2002-2016
 */
public class InstructionExecutor extends Instruction {

    public int read32(CodeInputStream input) {
        return read16(input) | (read16(input) << 16);
    }

    public int read16(CodeInputStream input) {
        return input.read() | (input.read() << 8);
    }

    public int readOdf(CodeExecutor executor, CodeInputStream input) {
        int bits = executor.getBits();
        if (bits == 16) {
            if (isHas67()) {
                bits = 32;
            }
        } else {
            if (isHas67()) {
                bits = 16;
            } else {
                bits = 32;
            }
        }
        if (bits == 16) {
            return read16(input);
        } else {
            return read32(input);
        }
    }

    public void executeJumpFar(CodeExecutor executor, CodeInputStream input, PC pc) {
        int offset = readOdf(executor, input);
        int base = read16(input);
        pc.cpu.regs.cs.setValue16(base, true);
        pc.cpu.regs.eip.setValue32(offset);
    }

}
