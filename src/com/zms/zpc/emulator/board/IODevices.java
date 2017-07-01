package com.zms.zpc.emulator.board;

import com.zms.zpc.emulator.PC;

/**
 * Created by 张小美 on 17/六月/28.
 * Copyright 2002-2016
 */
public class IODevices {

    public PC pc;

    public IODevices(PC pc) {
        this.pc=pc;
    }

    public void write(long address,long value,int width) {
        System.out.printf("address: %d\tvalue: %d\twidth: %d\n",address,value,width);
    }

}
