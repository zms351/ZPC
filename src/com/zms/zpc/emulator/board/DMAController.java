package com.zms.zpc.emulator.board;

/**
 * Created by 张小美 on 17/六月/28.
 * Copyright 2002-2016
 */
public class DMAController implements IODevice {

    public MotherBoard mb;

    public DMAController(MotherBoard mb) {
        this.mb=mb;
    }

    @Override
    public void write(int address, long v, int width) {

    }

    @Override
    public long read(int address, int width) {
        return 0;
    }

}
