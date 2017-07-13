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
    public RTC rtc;

    public MotherBoard(PC pc) {
        this.pc = pc;
        this.init();
    }

    private void init() {
        ios = new IODevices(this);
        dma1 = new DMAController(this);
        dma2 = new DMAController(this);
        rtc = new RTC(this);
    }

    public void reset() {
        rtc.reset();
    }

}
