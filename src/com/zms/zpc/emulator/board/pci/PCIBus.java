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
    public DefaultVGACard vga;

    private int biosIOAddress;
    private int biosMemoryAddress;
    private static final byte[] PCI_IRQS = new byte[]{11, 9, 11, 9};

    public PCIBus(MotherBoard mb) {
        this.mb = mb;
        this.init();
    }

    public void init() {
    }

    @Override
    public void reset() {
        biosIOAddress = 0xc000;
        biosMemoryAddress = 0xf0000000;
        byte elcr[] = new byte[2];

        /* activate IRQ mappings */
        elcr[0] = 0x00;
        elcr[1] = 0x00;
        for (int i = 0; i < 4; i++) {
            byte irq = PCI_IRQS[i];
            /* set to trigger level */
            elcr[irq >> 3] |= (1 << (irq & 7));
            /* activate irq remapping in PIIX */
            mb.isaBridge.configWrite(0x60 + i, irq, 8);
        }

        mb.ios.write(0x4d0, elcr[0], 8); // setup io master
        mb.ios.write(0x4d1, elcr[1], 8); // setup io slave

        /*for (int devFN = 0; devFN < 256; devFN++) {
            BasePCIDevice device = devices[devFN];
            if (device != null) {
                device.init();
            }
        }*/
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
        if(device instanceof DefaultVGACard) {
            vga= (DefaultVGACard) device;
        }
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

    public void writePCIData(long address, long data, int width) {
        BasePCIDevice device = this.validPCIDataAccess(address);
        if (null == device) {
            return;
        }
        if (device.configWrite(address & 0xff, data, width)) {
            this.updateMappings(device);
        }
    }

    protected void updateMappings(BasePCIDevice device) {
        //todo
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
