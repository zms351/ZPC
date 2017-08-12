package com.zms.zpc.emulator.store;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.board.helper.BaseDevice;

/**
 * Created by 张小美 on 17/八月/13.
 * Copyright 2002-2016
 */
public class FloppyDrive extends BaseDevice {

    public MotherBoard mb;
    public FloppyController.DriverType driver;

    public FloppyDrive(MotherBoard mb) {
        this.mb = mb;
        this.init();
    }

    protected void init() {
        driver=FloppyController.DriverType.DRIVE_144;
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

}
