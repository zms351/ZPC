package com.zms.zpc.emulator.debug;

import com.zms.zpc.emulator.board.*;
import com.zms.zpc.support.BaseObj;

import java.util.Arrays;

/**
 * Created by 张小美 on 17/六月/27.
 * Copyright 2002-2016
 */
public class DummyDebugger extends BaseObj implements IDebugger, IODevice {

    public static int[] Ports = new int[]{0x80, 0x400, 0x401, 0x402, 0x403};

    private static DummyDebugger instance = new DummyDebugger();

    public static DummyDebugger getInstance() {
        return instance;
    }

    public MotherBoard mb;

    public DummyDebugger(MotherBoard mb) {
        this.mb = mb;
        this.init();
    }

    private DummyDebugger() {
    }

    @Override
    public void onMessage(int type, String message, Object... params) {
        if (this != instance) {
            instance.onMessage(type, message, params);
            return;
        }
        if (type >= LOG) {
            if (BaseObj.Debug == 1) {
                if (type >= INFO) {
                    System.err.printf(message, params);
                }
            }
            return;
        }
        System.err.printf("%d:\t%s\n", type, message);
    }

    private void init() {
        for (int port : Ports) {
            mb.ios.register(port, this);
        }
        this.reset();
    }

    private StringBuilder[] builders;
    private long diag;

    @Override
    public void write(int address, long v, int width) {
        int index = Arrays.binarySearch(Ports, address);
        if (index > 0) {
            if (width == 8) {
                if (v == '\n') {
                    String s = builders[index].toString();
                    builders[index].setLength(0);
                    onMessage(INFO, "%s\n", s);
                } else {
                    builders[index].append((char) v);
                }
            }
        } else {
            diag = v;
        }
    }

    @Override
    public long read(int address, int width) {
        return 0;
    }

    @Override
    public void reset() {
        if (builders == null) {
            builders = new StringBuilder[Ports.length];
        }
        for (int i = 0; i < builders.length; i++) {
            if (builders[i] == null) {
                builders[i] = new StringBuilder();
            } else {
                builders[i].setLength(0);
            }
        }
    }

    public long getDiag() {
        return diag;
    }

}
