package com.zms.zpc.emulator.board.helper;

import com.zms.zpc.emulator.board.pci.PCIBus;
import com.zms.zpc.emulator.memory.*;

/**
 * Created by 张小美 on 17/八月/4.
 * Copyright 2002-2016
 */
public abstract class BasePCIDevice extends BaseDevice {

    public static final int PCI_CONFIG_VENDOR_ID = 0x00;
    public static final int PCI_CONFIG_DEVICE_ID = 0x02;
    public static final int PCI_CONFIG_COMMAND = 0x04;
    public static final int PCI_CONFIG_STATUS = 0x06;
    public static final int PCI_CONFIG_REVISION = 0x08;
    public static final int PCI_CONFIG_CLASS_DEVICE = 0x0a;
    public static final int PCI_CONFIG_CLS = 0x0c;
    public static final int PCI_CONFIG_LATENCY = 0x0d;
    public static final int PCI_CONFIG_HEADER = 0x0e;
    public static final int PCI_CONFIG_BIST = 0x0f;
    public static final int PCI_CONFIG_BASE_ADDRESS = 0x10;
    public static final int PCI_CONFIG_EXPANSION_ROM_BASE_ADDRESS = 0x30;
    public static final int PCI_CONFIG_INTERRUPT_LINE = 0x3c;
    public static final int PCI_CONFIG_INTERRUPT_PIN = 0x3d;
    public static final int PCI_CONFIG_MIN_GNT = 0x3e;
    public static final int PCI_CONFIG_MAX_LATENCY = 0x3f;
    public static final int PCI_COMMAND_IO = 0x1;
    public static final int PCI_COMMAND_MEMORY = 0x2;
    public static final int PCI_HEADER_PCI_PCI_BRIDGE = 0x01;
    public static final int PCI_HEADER_SINGLE_FUNCTION = 0x00;
    public static final int PCI_HEADER_MULTI_FUNCTION = 0x80;

    public PCIBus bus;
    public RAM configuration;

    public BasePCIDevice(PCIBus bus) {
        this.bus = bus;
        configuration = new PhysicalMemory(new byte[256]);
    }

    private int funcNum;

    public int getFuncNum() {
        return funcNum;
    }

    public void setFuncNum(int funcNum) {
        this.funcNum = funcNum;
    }

    public final void putConfig(long address,long data,int width) {
        configuration.write(0, address, data, width);
    }

    public final long configRead(long address,int width) {
        return configuration.read(0, address, width);
    }

    public final boolean configWrite(long address, long data,int width) {
        //todo
        return true;
    }

}
