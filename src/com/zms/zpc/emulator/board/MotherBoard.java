package com.zms.zpc.emulator.board;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.board.pci.*;
import com.zms.zpc.emulator.board.time.*;
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
    public PCSpeaker speaker;
    public IntervalTimer timer;
    public DummyDebugger debugger;
    public PCIBus pciBus;
    public PCIISABridge isaBridge;

    public InterruptController pic;
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

        pic=new InterruptController(this);
        devices.add(pic);

        dma1 = new DMAController(this,false,true);
        devices.add(dma1);

        dma2 = new DMAController(this,false,false);
        devices.add(dma2);
        dma1.slave=dma2;

        dma1.memory=pc.memory;

        keyboard = new Keyboard(this);
        devices.add(keyboard);

        vc=new VirtualClock(this,false);

        timer=new IntervalTimer(this,0x40,0);
        devices.add(timer);

        speaker=new PCSpeaker(this);
        devices.add(speaker);
        timer.speaker=speaker;

        debugger = new DummyDebugger(this);
        devices.add(debugger);

        pciBus = new PCIBus(this);
        devices.add(pciBus);

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
