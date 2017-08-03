package com.zms.zpc.emulator.board.pci;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.board.helper.BasePCIDevice;
import com.zms.zpc.support.NotImplException;

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

    public int[] ports = new int[]{0xcf8, 0xcfc, 0xcfd, 0xcfe, 0xcff};

    public void init() {
        for (int port : ports) {
            mb.ios.register(port, this);
        }
        mb.pciBus.addDevice(this);

        setFuncNum(0);
        putConfig(PCI_CONFIG_VENDOR_ID, 0x8086, 16); // vendor_id
        putConfig(PCI_CONFIG_DEVICE_ID, 0x1237, 16); // device_id
        putConfig(PCI_CONFIG_REVISION, 0x02, 8); // revision
        putConfig(PCI_CONFIG_CLASS_DEVICE, 0x0600, 16); // pci host bridge
        putConfig(PCI_CONFIG_HEADER, 0x00, 8); // header_type

        reset();
    }

    @Override
    public void reset() {
    }

    private long configRegister;

    @Override
    public void write(int address, long v, int width) {
        switch (width) {
            case 32:
                switch (address) {
                    case 0xcf8:
                        configRegister = v;
                        break;
                }
                break;
        }
    }

    @Override
    public long read(int address, int width) {
        switch (address) {
            case 0xcfc:
            case 0xcfd:
            case 0xcfe:
            case 0xcff:
                if ((configRegister & Pows[31]) == 0L) {
                    return 0xffffffffffffffffL;
                } else {
                    return bus.readPCIData(configRegister | (address & 0x3), width);
                }
        }
        throw new NotImplException();
    }

}
