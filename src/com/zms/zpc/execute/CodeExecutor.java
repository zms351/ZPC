package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.processor.Regs;
import com.zms.zpc.support.NotImplException;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public class CodeExecutor {

    private int bits = 16;
    private byte[] previewBuffer;
    private InstructionExecutor instruction;

    public CodeExecutor() {
        previewBuffer = new byte[16];
        instruction = new InstructionExecutor();
    }

    protected void pre(PC pc) {
        Regs regs = pc.cpu.regs;
        if (regs.bits.pe.get()) {
            throw new NotImplException();
        } else {
            bits = 16;
        }
    }

    public int execute(PC pc, CodeInputStream input) {
        pre(pc);
        instruction.setStartPos(input.getPos());
        instruction.parse1(input, bits);
        switch (instruction.getOpcode()[0]) {
            case 0xea:
                instruction.executeJumpFar(this, input, pc);
                break;
            default:
                throw new NotImplException();
        }
        return 0;
    }

    public String decode(PC pc, CodeInputStream input) {
        pre(pc);
        long pos = input.getPos();
        try {
            input.readFully(previewBuffer);
        } finally {
            input.setPos(pos);
        }
        return Disassembler.nativeAssemble(previewBuffer, bits);
    }

    public int getBits() {
        return bits;
    }

    public void setBits(int bits) {
        this.bits = bits;
    }

}
