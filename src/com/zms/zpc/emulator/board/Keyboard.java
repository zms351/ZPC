package com.zms.zpc.emulator.board;

/**
 * Created by 张小美 on 17/七月/23.
 * Copyright 2002-2016
 */
public class Keyboard implements IODevice {

    public MotherBoard mb;

    public Keyboard(MotherBoard mb) {
        this.mb = mb;
        this.init();
    }

    /* Status Register Bits */
    private static final byte KBD_STAT_OBF = (byte)0x01; /* Keyboard output buffer full */
    private static final byte KBD_STAT_IBF = (byte)0x02; /* Keyboard input buffer full */
    private static final byte KBD_STAT_SELFTEST = (byte)0x04; /* Self test successful */
    private static final byte KBD_STAT_CMD = (byte)0x08; /* Last write was a command write (0=data) */
    private static final byte KBD_STAT_UNLOCKED = (byte)0x10; /* Zero if keyboard locked */
    private static final byte KBD_STAT_MOUSE_OBF = (byte)0x20; /* Mouse output buffer full */
    private static final byte KBD_STAT_GTO = (byte)0x40; /* General receive/xmit timeout */
    private static final byte KBD_STAT_PERR = (byte)0x80; /* Parity error */

    public static final int IOA = 0x60;
    public static final int IOB = 0x64;

    public int status;

    private void init() {
        IODevices ios = mb.ios;
        ios.register(IOA, this);
        ios.register(IOB, this);
        this.reset();
    }

    @Override
    public void write(int address, long v, int width) {
    }

    @Override
    public long read(int address, int width) {
        if(address==IOB) {
            if(width==8) {
                return status;
            }
        }
        return 0;
    }

    public void reset() {
        status = (byte)(KBD_STAT_CMD | KBD_STAT_UNLOCKED);
    }

}
