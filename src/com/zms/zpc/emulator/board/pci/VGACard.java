package com.zms.zpc.emulator.board.pci;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.board.helper.BasePCIDevice;

/**
 * Created by 张小美 on 17/八月/4.
 * Copyright 2002-2016
 */
public abstract class VGACard extends BasePCIDevice {

    public MotherBoard mb;

    public VGACard(MotherBoard mb) {
        super(mb.pciBus);
        this.mb = mb;
        this.init();
    }

    public void init() {
        mb.pciBus.addDevice(this);

        setFuncNum(-1);
        putConfig(PCI_CONFIG_VENDOR_ID, 0x1234, 16); // Dummy
        putConfig(PCI_CONFIG_DEVICE_ID, 0x1111, 16);
        putConfig(PCI_CONFIG_CLASS_DEVICE, 0x0300, 16); // VGA Controller
        putConfig(PCI_CONFIG_HEADER, 0x00, 8); // header_type

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
