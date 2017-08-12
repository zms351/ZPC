package com.zms.zpc.emulator.board.pci;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.board.helper.BasePCIDevice;

/**
 * Created by 张小美 on 17/八月/4.
 * Copyright 2002-2016
 */
public class PCIISABridge extends BasePCIDevice {

    public MotherBoard mb;

    public PCIISABridge(MotherBoard mb) {
        super(mb.pciBus);
        this.mb = mb;
        this.init();
    }

    public void init() {
        setFuncNum(-1);
        putConfig(PCI_CONFIG_VENDOR_ID, 0x8086, 16); // Intel
        putConfig(PCI_CONFIG_DEVICE_ID, 0x7000, 16); // 82371SB PIIX3 PCI-to-ISA bridge (Step A1)
        putConfig(PCI_CONFIG_CLASS_DEVICE, 0x0601, 16); // ISA Bridge
        putConfig(PCI_CONFIG_HEADER, 0x80, 8); // PCI_multifunction

        reset();
        mb.pciBus.addDevice(this);
    }

    @Override
    public void ioPortWrite8(int address, int data) {

    }

    @Override
    public void ioPortWrite16(int address, int data) {

    }

    @Override
    public void ioPortWrite32(int address, int data) {

    }

    @Override
    public int ioPortRead8(int address) {
        return 0;
    }

    @Override
    public int ioPortRead16(int address) {
        return 0;
    }

    @Override
    public int ioPortRead32(int address) {
        return 0;
    }

    @Override
    public int[] ioPortsRequested() {
        return new int[0];
    }

    @Override
    public void reset() {
    }

}
