package com.zms.zpc.emulator.board;

import com.zms.zpc.support.NotImplException;

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

    private byte[] cmos;
    private int address;

    public static final int IOA = 0x70;
    public static final int IOB = 0x71;

    private void init() {
        IODevices ios = mb.ios;
        ios.register(IOA, this);
        ios.register(IOB, this);

        cmos = new byte[128];
        cmos[0xf] = 0; //shutdown status
    }

    @Override
    public void write(int _address, long v, int width) {
        if (width != 8) {
            throw new NotImplException();
        }
        if (_address == IOA) {
            this.address = ((int) (v)) & 0xff;
        } else if (_address == IOB) {
            boolean allow = true;
            switch (this.address) {

            }
            if (allow) {
                cmos[this.address] = (byte) v;
            }
        } else {
            throw new NotImplException();
        }
    }

    @Override
    public long read(int _address, int width) {
        if (width != 8) {
            throw new NotImplException();
        }
        if (_address == IOA) {
            return 0xff;
        } else if (_address == IOB) {
            return cmos[this.address];
        } else {
            throw new NotImplException();
        }
    }

}
