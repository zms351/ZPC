package com.zms.zpc.emulator.board.helper;

import com.zms.zpc.support.NotImplException;

/**
 * Created by 张小美 on 17/八月/13.
 * Copyright 2002-2016
 */
public abstract class BaseIODevice extends BaseDevice {

    public abstract void ioPortWrite8(int address, int data);

    public abstract void ioPortWrite16(int address, int data);

    public abstract void ioPortWrite32(int address, int data);

    public abstract int ioPortRead8(int address);

    public abstract int ioPortRead16(int address);

    public abstract int ioPortRead32(int address);

    public abstract int[] ioPortsRequested();

    @Override
    public long read(int address, int width) {
        switch (width) {
            case 8:
                return ioPortRead8(address);
            case 16:
                return ioPortRead16(address);
            case 32:
                return ioPortRead32(address);
            default:
                throw new NotImplException();
        }
    }

    @Override
    public void write(int address, long v, int width) {
        switch (width) {
            case 8:
                ioPortWrite8(address, (int) v);
                break;
            case 16:
                ioPortWrite16(address, (int) v);
                break;
            case 32:
                ioPortWrite32(address, (int) v);
            default:
                throw new NotImplException();
        }
    }

}
