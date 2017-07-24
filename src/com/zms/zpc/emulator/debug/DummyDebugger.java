package com.zms.zpc.emulator.debug;

import com.zms.zpc.emulator.board.*;
import com.zms.zpc.support.BaseObj;

/**
 * Created by 张小美 on 17/六月/27.
 * Copyright 2002-2016
 */
public class DummyDebugger extends BaseObj implements IDebugger, IODevice {

    public MotherBoard mb;

    public DummyDebugger(MotherBoard mb) {
        this.mb = mb;
        this.init();
    }

    public DummyDebugger() {
    }

    @Override
    public void onMessage(int type, String message, Object... params) {
        if (type == DEBUG) {
            if (Debug == 1) {
                System.err.printf(message, params);
            }
        } else {
            System.err.printf("%d:\t%s\n", type, message);
        }
    }

    public static int[] Ports = new int[]{0x80, 0x400, 0x401, 0x402, 0x403};

    private void init() {
        for (int port : Ports) {
            mb.ios.register(port, this);
        }
        this.reset();
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
