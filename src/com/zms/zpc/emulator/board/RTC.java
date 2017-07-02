package com.zms.zpc.emulator.board;

/**
 * MC146818
 * <p>
 * Created by 张小美 on 17/七月/2.
 * Copyright 2002-2016
 */
public class RTC implements IODevice {

    public MotherBoard mb;

    public RTC(MotherBoard mb) {
        this.mb = mb;
        this.init();
    }

    private void init() {
        IODevices ios = mb.ios;
        ios.register(0x70, this);
        ios.register(0x71, this);
    }

    @Override
    public void write(int address, long v, int width) {
        System.out.println("here 1");
    }

    @Override
    public long read(int address, int width) {
        System.out.println("here 2");
        return 0;
    }

}
