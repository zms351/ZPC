package com.zms.zpc.emulator.board;

import com.zms.zpc.emulator.board.helper.BaseDevice;
import com.zms.zpc.support.NotImplException;

/**
 * Created by 张小美 on 17/六月/28.
 * Copyright 2002-2016
 */
public class DMAController extends BaseDevice {

    public MotherBoard mb;

    public DMAController(MotherBoard mb) {
        this.mb = mb;
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

    public void releaseDmaRequest(int channel) {
        throw new NotImplException();
    }

    public void holdDmaRequest(int channel) {
        throw new NotImplException();
    }

    public int getChannelMode(int channel) {
        throw new NotImplException();
    }

}
