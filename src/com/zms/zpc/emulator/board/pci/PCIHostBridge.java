package com.zms.zpc.emulator.board.pci;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.board.helper.BasePCIDevice;

/**
 * Created by 张小美 on 17/八月/4.
 * Copyright 2002-2016
 */
public class PCIHostBridge extends BasePCIDevice {

    public MotherBoard mb;

    public PCIHostBridge(MotherBoard mb) {
        super(mb.pciBus);
        this.mb = mb;
        this.init();
    }

    public void init() {
        for (int port : ioPortsRequested()) {
            mb.ios.register(port, this);
        }

        setFuncNum(0);
        putConfig(PCI_CONFIG_VENDOR_ID, 0x8086, 16); // vendor_id
        putConfig(PCI_CONFIG_DEVICE_ID, 0x1237, 16); // device_id
        putConfig(PCI_CONFIG_REVISION, 0x02, 8); // revision
        putConfig(PCI_CONFIG_CLASS_DEVICE, 0x0600, 16); // pci host bridge
        putConfig(PCI_CONFIG_HEADER, 0x00, 8); // header_type

        reset();
        mb.pciBus.addDevice(this);
    }

    @Override
    public void reset() {
    }

    private long configRegister;

    public int ioPortRead8(int address) {
        switch (address) {
            case 0xcfc:
            case 0xcfd:
            case 0xcfe:
            case 0xcff:
                if ((configRegister & Pows[31]) == 0)
                    return 0xff;
                else
                    return 0xff & (int) bus.readPCIData(configRegister | (address & 0x3), 8);

            default:
                return 0xff;
        }
    }

    public int ioPortRead16(int address) {
        switch (address) {
            case 0xcfc:
            case 0xcfd:
            case 0xcfe:
            case 0xcff:
                if ((configRegister & Pows[31]) == 0)
                    return 0xffff;
                else
                    return 0xffff & (int) bus.readPCIData(configRegister | (address & 0x3), 16);
            default:
                return 0xffff;
        }
    }

    public int ioPortRead32(int address) {
        switch (address) {
            case 0xcf8:
            case 0xcf9:
            case 0xcfa:
            case 0xcfb:
                return (int) configRegister;
            case 0xcfc:
            case 0xcfd:
            case 0xcfe:
            case 0xcff:
                if ((configRegister & Pows[31]) == 0)
                    return 0xffffffff;
                else
                    return (int) bus.readPCIData(configRegister | (address & 0x3), 32);
            default:
                return 0xffffffff;
        }
    }

    public void ioPortWrite8(int address, int data) {
        switch (address) {
            case 0xcfc:
            case 0xcfd:
            case 0xcfe:
            case 0xcff:
                if ((configRegister & Pows[31]) != 0)
                    bus.writePCIData(configRegister | (address & 0x3), data, 8);
                break;
            default:
        }
    }

    public void ioPortWrite16(int address, int data) {
        switch (address) {
            case 0xcfc:
            case 0xcfd:
            case 0xcfe:
            case 0xcff:
                if ((configRegister & Pows[31]) != 0)
                    bus.writePCIData(configRegister | (address & 0x3), data, 16);
                break;
            default:
        }
    }

    public void ioPortWrite32(int address, int data) {
        switch (address) {
            case 0xcf8:
            case 0xcf9:
            case 0xcfa:
            case 0xcfb:
                configRegister = data;
                break;
            case 0xcfc:
            case 0xcfd:
            case 0xcfe:
            case 0xcff:
                if ((configRegister & Pows[31]) != 0)
                    bus.writePCIData(configRegister | (address & 0x3), data, 32);
                break;
            default:
        }
    }

    public int[] ioPortsRequested() {
        return new int[]{0xcf8, 0xcf9, 0xcfa, 0xcfb, 0xcfc, 0xcfd, 0xcfe, 0xcff};
    }

}
