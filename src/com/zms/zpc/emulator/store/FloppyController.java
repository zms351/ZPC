package com.zms.zpc.emulator.store;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.board.helper.BaseDevice;

/**
 * Created by 张小美 on 17/八月/13.
 * Copyright 2002-2016
 */
public class FloppyController extends BaseDevice {

    public MotherBoard mb;
    private FloppyDrive[] drivers;

    public FloppyController(MotherBoard mb) {
        this.mb = mb;
        this.init();
    }

    private static final int IOPORT_BASE = 0x3f0;

    public int[] ioPortsRequested() {
        return new int[]{IOPORT_BASE + 1, IOPORT_BASE + 2, IOPORT_BASE + 3,
                IOPORT_BASE + 4, IOPORT_BASE + 5, IOPORT_BASE + 7};
    }

    protected void init() {
        drivers = new FloppyDrive[2];
        drivers[0] = new FloppyDrive(mb);
        for (int port : ioPortsRequested()) {
            mb.ios.register(port,this);
        }
        reset();
    }

    @Override
    public void write(int address, long v, int width) {

    }

    @Override
    public long read(int address, int width) {
        return 0;
    }

    @Override
    public void reset() {

    }

    public DriverType getDriveType(int number) {
        if (drivers[number] == null) {
            return DriverType.DRIVE_NONE;
        }
        return drivers[number].driver;
    }

    public enum DriverType {DRIVE_144, DRIVE_288, DRIVE_120, DRIVE_NONE}

}
