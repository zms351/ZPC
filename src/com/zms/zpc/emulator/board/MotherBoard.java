package com.zms.zpc.emulator.board;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.board.pci.*;
import com.zms.zpc.emulator.debug.DummyDebugger;

import java.util.*;

/**
 * Created by 张小美 on 17/六月/28.
 * Copyright 2002-2016
 */
public class MotherBoard {

    public PC pc;
    public IODevices ios;

    public DMAController dma1;
    public DMAController dma2;
    public CMOS cmos;
    public Keyboard keyboard;
    public DummyDebugger debugger;
    public PCIBus pciBus;

    public MotherBoard(PC pc) {
        this.pc = pc;
        devices = new ArrayList<>();
        this.init();
    }

    public List<IODevice> devices;

    private void init() {
        ios = new IODevices(this);

        devices.clear();

        dma1 = new DMAController(this);
        devices.add(dma1);

        dma2 = new DMAController(this);
        devices.add(dma2);

        cmos = new CMOS(this);
        devices.add(cmos);

        keyboard = new Keyboard(this);
        devices.add(keyboard);

        debugger = new DummyDebugger(this);
        devices.add(debugger);

        pciBus = new PCIBus(this);
        devices.add(pciBus);

        devices.add(new PCIHostBridge(this));
    }

    public void reset() {
        for (IODevice device : devices) {
            device.reset();
        }
    }

}
