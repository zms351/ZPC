package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public class CodeExecutor {

    private int bits=16;

    public int execute(PC pc,CodeInputStream input) {
        return 0;
    }

    public int getBits() {
        return bits;
    }

    public void setBits(int bits) {
        this.bits = bits;
    }

}
