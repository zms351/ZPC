package com.zms.zpc.emulator.board;

import com.zms.zpc.emulator.PC;

/**
 * Created by 张小美 on 17/六月/28.
 * Copyright 2002-2016
 */
public class MotherBoard {

    public PC pc;
    public IODevices ios;
    public DMAController dma1;
    public DMAController dma2;

    public MotherBoard(PC pc) {
        this.pc = pc;
        this.init();
    }

    private void init() {
        ios = new IODevices(pc);
        dma1 = new DMAController();
        dma2 = new DMAController();
    }

}
