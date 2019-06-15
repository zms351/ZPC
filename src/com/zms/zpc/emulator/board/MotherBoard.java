package com.zms.zpc.emulator.board;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.board.pci.*;
import com.zms.zpc.emulator.board.time.VirtualClock;
import com.zms.zpc.emulator.debug.DummyDebugger;
import com.zms.zpc.emulator.store.FloppyController;

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
    public PCIISABridge isaBridge;

    public SimpleInterruptController pic;
    public VirtualClock vc;
    public FloppyController floppy;

    public MotherBoard(PC pc) {
        this.pc = pc;
        devices = new ArrayList<>();
        this.init();
    }

    public List<IODevice> devices;

    private void init() {
        ios = new IODevices(this);

        devices.clear();

        pic=new SimpleInterruptController(this);
        devices.add(pic);

        dma1 = new DMAController(this);
        devices.add(dma1);

        dma2 = new DMAController(this);
        devices.add(dma2);

        keyboard = new Keyboard(this);
        devices.add(keyboard);

        debugger = new DummyDebugger(this);
        devices.add(debugger);

        pciBus = new PCIBus(this);
        devices.add(pciBus);

        vc=new VirtualClock(this);

        devices.add(new PCIHostBridge(this));
        devices.add(new DefaultVGACard(this));
        devices.add(isaBridge=new PCIISABridge(this));
        devices.add(new PIIX3IDEInterface(this));

        floppy=new FloppyController(this);
        devices.add(floppy);

        cmos = new CMOS(this);
        devices.add(cmos);
    }

    public void reset() {
        for (IODevice device : devices) {
            device.reset();
        }
    }

}
