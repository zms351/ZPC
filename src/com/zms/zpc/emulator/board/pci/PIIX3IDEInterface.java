package com.zms.zpc.emulator.board.pci;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.board.helper.BasePCIDevice;

/**
 * Created by 张小美 on 17/八月/4.
 * Copyright 2002-2016
 */
public class PIIX3IDEInterface extends BasePCIDevice {

    public MotherBoard mb;

    public PIIX3IDEInterface(MotherBoard mb) {
        super(mb.pciBus);
        this.mb = mb;
        this.init();
    }

    public void init() {
        setFuncNum(11);
        putConfig(PCI_CONFIG_VENDOR_ID, 0x8086, 16); // Intel
        putConfig(PCI_CONFIG_DEVICE_ID, 0x7010, 16);
        putConfig(0x09, 0x80, 8); // legacy ATA mode
        putConfig(PCI_CONFIG_CLASS_DEVICE, 0x0101, 16); // PCI IDE
        putConfig(PCI_CONFIG_HEADER, 0x00, 8); // header_type

        reset();
        mb.pciBus.addDevice(this);
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
