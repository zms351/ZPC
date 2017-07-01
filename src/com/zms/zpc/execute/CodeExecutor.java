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
    private Regs regs;

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

    public int execute(PC pc, CodeStream input) {
        pre(pc);
        instruction.setStartPos(input.getPos());
        instruction.parse1(input, bits);
        regs = pc.cpu.regs;
        instruction.bits = regs.bits;
        boolean jump = false;
        switch (instruction.getOpcode()[0]) {
            case 0xea:
                //org.jpc.emulator.execution.opcodes.rm.jmp_Ap
                instruction.executeJumpFar(this, input, pc);
                jump = true;
                break;
            case 0x30:
                //org.jpc.emulator.execution.opcodes.rm.xor_Eb_Gb_mem
                //org.jpc.emulator.execution.opcodes.rm.xor_Ew_Gw
                instruction.parse2(input, bits);
                instruction.executeXor30313233(this, input, pc, false);
                break;
            case 0x31:
                //org.jpc.emulator.execution.opcodes.rm.xor_Ew_Gw_mem
                //org.jpc.emulator.execution.opcodes.rm.xor_Ew_Gw
                instruction.parse2(input, bits);
                instruction.executeXor30313233(this, input, pc, false);
                break;
            case 0x32:
                instruction.parse2(input, bits);
                instruction.executeXor30313233(this, input, pc, true);
                break;
            case 0x33:
                instruction.parse2(input, bits);
                instruction.executeXor30313233(this, input, pc, true);
                break;
            case 0xe6:
            case 0xe7:
            case 0xee:
            case 0xef:
                instruction.executeOut(this, input, pc);
                break;
            default:
                throw new NotImplException();
        }
        if (!jump) {
            reLoc(input);
        }
        return 0;
    }

    private void reLoc(CodeStream input) {
        regs.rip.setValue64(regs.rip.getValue64() + (input.getPos() - instruction.getStartPos()));
    }

    public String decode(PC pc, CodeStream input) {
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
