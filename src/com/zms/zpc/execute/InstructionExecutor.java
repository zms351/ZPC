package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.processor.Bits;
import com.zms.zpc.support.*;

/**
 * Created by 张小美 on 17/六月/27.
 * Copyright 2002-2016
 */
public class InstructionExecutor extends Instruction implements Constants {

    public Bits bits;

    public long readOp(CodeExecutor executor, CodeStream input) {
        int width = getOpWidth(executor.getBits());
        switch (width) {
            case 8:
                return input.read();
            case 16:
                return read16(input);
            case 32:
                return read32(input);
            case 64:
                return read64(input);
        }
        throw new NotImplException();
    }

    public void executeJumpFar(CodeExecutor executor, CodeStream input, PC pc) {
        long offset = readOp(executor, input);
        int base = read16(input);
        pc.cpu.regs.cs.setValue16(base, true);
        pc.cpu.regs.rip.setValue64(offset);
    }

    public void executeXor30313233(CodeExecutor executor, CodeStream input, PC pc, boolean rm) {
        long v1 = mrs.getValMemory(pc);
        long v2 = mrs.getValReg(pc);
        long v = v1 ^ v2;
        bits.clearOCA();
        bits.result = v;
        if (rm) {
            mrs.setValReg(pc, v);
        } else {
            mrs.setValMemory(pc, v);
        }
        bits.status = SZP;
    }

}
