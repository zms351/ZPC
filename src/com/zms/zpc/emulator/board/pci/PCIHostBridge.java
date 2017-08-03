package com.zms.zpc.emulator.board.pci;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.board.helper.BaseDevice;

/**
 * Created by 张小美 on 17/八月/4.
 * Copyright 2002-2016
 */
public class PCIHostBridge extends BaseDevice {

    public MotherBoard mb;

    public PCIHostBridge(MotherBoard mb) {
        this.mb = mb;
        this.init();
    }

    public int[] ports = new int[]{0xcf8, /*0xcf9, 0xcfa, 0xcfb, */ 0xcfc};

    public void init() {
        for (int port : ports) {
            mb.ios.register(port,this);
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void write(int address, long v, int width) {
    }

    @Override
    public long read(int address, int width) {
        return 0;
    }

}
