package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;

/**
 * Created by 张小美 on 17/六月/27.
 * Copyright 2002-2016
 */
public class InstructionExecutor extends Instruction {

    public int readOdf(CodeExecutor executor,CodeInputStream input) {
        int bits=executor.getBits();
        assert bits==16 || bits==32;
        return 0;
    }

    public void executeJumpFar(CodeExecutor executor,CodeInputStream input,PC pc) {

    }

}
