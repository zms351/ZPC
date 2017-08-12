package com.zms.zpc.emulator.store;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.board.helper.BaseDevice;

/**
 * Created by 张小美 on 17/八月/13.
 * Copyright 2002-2016
 */
public class FloppyDrive extends BaseDevice {

    static final int MOTOR_ON = 0x01; // motor on/off

    static final int REVALIDATE = 0x02; // Revalidated

    static final int DOUBLE_SIDES = 0x01;

    public MotherBoard mb;
    public FloppyController.DriverType driver;

    int driveFlags;
    int perpendicular;
    int head;
    int headCount;
    int track;
    int sector;
    int sectorCount;
    int direction;
    int readWrite;
    int flags;
    int maxTrack;
    int bps;
    int readOnly;

    public FloppyDrive(MotherBoard mb) {
        this.mb = mb;
        this.init();
    }

    protected void init() {
        driver = FloppyController.DriverType.DRIVE_144;
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

    public void start() {
        driveFlags |= MOTOR_ON;
    }

    public void stop() {
        driveFlags &= ~MOTOR_ON;
    }

}
