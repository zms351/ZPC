package com.zms.zpc.emulator.board;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.support.*;

/**
 * Created by 张小美 on 17/六月/28.
 * Copyright 2002-2016
 */
public class IODevices extends BaseObj {

    public MotherBoard mb;
    public PC pc;
    public IODevice[] devices;

    public IODevices(MotherBoard mb) {
        this.mb = mb;
        this.pc = mb.pc;
        devices = new IODevice[0x10000];
    }

    public void write(int address, long value, int width) {
        IODevice device = devices[address];
        if (device != null) {
            device.write(address, value, width);
        } else {
            pc.getDebugger().onMessage(WARN,"io write,address: %h\tvalue: %d\twidth: %d\n", address, value, width);
        }
    }

    public long read(int address,int width) {
        IODevice device = devices[address];
        if (device != null) {
            return device.read(address,width);
        } else {
            pc.getDebugger().onMessage(WARN,"io read,address: %h\twidth: %d\n", address, width);
            return 0;
        }
    }

    public void register(int address, IODevice device) {
        devices[address] = device;
    }

}
