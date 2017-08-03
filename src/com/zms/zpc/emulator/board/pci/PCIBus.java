package com.zms.zpc.emulator.board.pci;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.board.helper.BaseDevice;

/**
 * Created by 张小美 on 17/八月/4.
 * Copyright 2002-2016
 */
public class PCIBus extends BaseDevice {

    public MotherBoard mb;

    public PCIBus(MotherBoard mb) {
        this.mb = mb;
        this.init();
    }

    public void init() {
    }

    @Override
    public void reset() {
    }

    @Override
    public void write(int address, long v, int width) {
    }

    @Override
    public long read(int address, int width) {
        return 0;
    }

    public void addDevice(BaseDevice device) {

    }

}
