package com.zms.zpc.emulator.board.pci;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.board.helper.*;

/**
 * Created by 张小美 on 17/八月/4.
 * Copyright 2002-2016
 */
public class PCIBus extends BaseDevice {

    public MotherBoard mb;
    public BasePCIDevice[] devices = new BasePCIDevice[256];

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

    public void addDevice(BasePCIDevice device) {
        if (device.getFuncNum() < 0) {
            int fn = findFreeDevFN();
            assert fn >= 0;
            device.setFuncNum(fn);
        }
        devices[device.getFuncNum()] = device;
    }

    private BasePCIDevice validPCIDataAccess(long address) {
        long bus = (address >>> 16) & 0xff;
        if (0 != bus) {
            return null;
        }
        return this.devices[(int) ((address >>> 8) & 0xff)];
    }

    public long readPCIData(long address, int width) {
        BasePCIDevice device = this.validPCIDataAccess(address);
        if (null == device) {
            return 0xffffffffffffffffL;
        }
        return device.configRead(address & 0xff, width);
    }

    private int findFreeDevFN() {
        for (int i = 8; i < 256; i += 8) {
            if (null == devices[i]) {
                return i;
            }
        }
        return -1;
    }

}
