package com.zms.zpc.emulator.board;

import com.zms.zpc.emulator.PC;

import java.util.*;

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
    public Keyboard keyboard;

    public MotherBoard(PC pc) {
        this.pc = pc;
        devices=new ArrayList<>();
        this.init();
    }

    public List<IODevice> devices;

    private void init() {
        ios = new IODevices(this);

        devices.clear();

        dma1 = new DMAController(this);
        devices.add(dma1);

        dma2 = new DMAController(this);
        devices.add(dma2);

        rtc = new RTC(this);
        devices.add(rtc);

        keyboard=new Keyboard(this);
        devices.add(keyboard);
    }

    public void reset() {
        for (IODevice device : devices) {
            device.reset();
        }
    }

}
