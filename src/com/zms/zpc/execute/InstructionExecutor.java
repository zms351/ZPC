package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.processor.Bits;
import com.zms.zpc.emulator.reg.BaseReg;
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

    public void executeOut(CodeExecutor executor, CodeStream input, PC pc) {
        int op = getOpcode();
        int width = getOpWidth(executor.getBits());
        if (op == 0xe6 || op == 0xee) {
            width = 8;
        }
        if (width != 8 && width != 16 && width != 32) {
            throw new NotImplException();
        }
        long a;
        if (op == 0xe6 || op == 0xe7) {
            a = input.read();
        } else {
            a = getReg(pc, "DX").getValue();
        }
        long v = getReg(pc, "RAX").getValue();
        pc.board.ios.write(a, v, width);
    }

    public void executeMov8ri(CodeExecutor executor, CodeStream input, PC pc) {
        int v=input.read();
        int r=getOpcode()-0xb0;
        BaseReg reg = getReg(pc, mrs.getR8(r));
        reg.setValue8(v);
    }

}
