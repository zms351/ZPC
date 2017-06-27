package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.assembler.Disassembler;
import com.zms.zpc.emulator.processor.Regs;
import com.zms.zpc.support.NotImplException;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public class CodeExecutor {

    private int bits = 16;
    private byte[] previewBuffer;

    public CodeExecutor() {
        previewBuffer = new byte[16];
    }

    public int execute(PC pc, CodeInputStream input) {
        Regs regs = pc.cpu.regs;
        if (regs.bits.pe.get()) {
            throw new NotImplException();
        } else {
            bits = 16;
        }
        {
            long pos = input.getPos();
            try {
                input.readFully(previewBuffer);
            } finally {
                input.setPos(pos);
            }
            String output = Disassembler.nativeAssemble(previewBuffer, bits);
        }
        //todo
        return 0;
    }

    public int getBits() {
        return bits;
    }

    public void setBits(int bits) {
        this.bits = bits;
    }

}
