package com.zms.zpc.emulator.board;

import com.zms.zpc.support.NotImplException;

/**
 * MC146818
 * <p>
 * Created by 张小美 on 17/七月/2.
 * Copyright 2002-2016
 */
public class CMOS implements IODevice {

    public MotherBoard mb;

    public CMOS(MotherBoard mb) {
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
        this.reset();
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

    public void reset() {
        cmos[0xf] = 0x0; //shutdown status

        //ram size
        long ramSize = mb.pc.memory.getTotalSize();
        long val = (ramSize / 1024) - 1024;
        if (val > 65535) {
            val = 65535;
        }
        cmos[0x17] = (byte) val;
        cmos[0x18] = (byte) (val >>> 8);
        cmos[0x30] = (byte) val;
        cmos[0x31] = (byte) (val >>> 8);

        if (ramSize > (16 * 1024 * 1024)) {
            val = (ramSize / 65536) - ((16 * 1024 * 1024) / 65536);
        } else {
            val = 0;
        }
        if (val > 65535) val = 65535;
        cmos[0x34] = (byte) val;
        cmos[0x35] = (byte) (val >>> 8);
    }

}
