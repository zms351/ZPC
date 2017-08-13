package com.zms.zpc.emulator.board;

import com.zms.zpc.emulator.board.helper.BaseDevice;
import com.zms.zpc.emulator.store.FloppyController;
import com.zms.zpc.support.NotImplException;

/**
 * MC146818
 * <p>
 * Created by 张小美 on 17/七月/2.
 * Copyright 2002-2016
 */
public class CMOS extends BaseDevice {

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
        cmos[0x3d] = (byte) 0x01; /* floppy boot */

        cmosInitFloppy(mb.floppy);
    }

    private void cmosInitFloppy(FloppyController fdc) {
        byte[] cmosData = cmos;
        int val = (cmosGetFDType(fdc, 0) << 4) | cmosGetFDType(fdc, 1);
        cmosData[0x10] = (byte) val;

        int num = 0;
        val = 0;
        if (fdc.getDriveType(0) != FloppyController.DriveType.DRIVE_NONE)
            num++;
        if (fdc.getDriveType(1) != FloppyController.DriveType.DRIVE_NONE)
            num++;
        switch (num) {
            case 0:
                break;
            case 1:
                val |= 0x01;
                break;
            case 2:
                val |= 0x41;
                break;
        }
        val |= 0x02; // Have FPU
        val |= 0x04; // Have PS2 Mouse
        cmosData[0x14] = (byte) val;
    }

    private int cmosGetFDType(FloppyController fdc, int drive) {
        switch (fdc.getDriveType(drive)) {
            case DRIVE_144:
                return 4;
            case DRIVE_288:
                return 5;
            case DRIVE_120:
                return 2;
            default:
                return 0;
        }
    }

}
