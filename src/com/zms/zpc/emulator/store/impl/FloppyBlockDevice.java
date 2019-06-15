package com.zms.zpc.emulator.store.impl;

import com.zms.zpc.emulator.store.SeekableIODevice;

/**
 * Created by 张小美 on 2019-06-09.
 * Copyright 2002-2016
 */
public class FloppyBlockDevice extends RawBlockDevice {

    /**
     * Constructs an instance backed by the given device.
     *
     * @param data backing device
     */
    public FloppyBlockDevice(SeekableIODevice data) {
        super(data);
    }

    /**
     * Returns <code>false</code> as a floppy drive cannot be locked
     *
     * @return <code>false</code>
     */
    public boolean isLocked() {
        return false;
    }

    /**
     * Does nothing, a floppy drive cannot be locked
     *
     * @param locked dummy
     */
    public void setLock(boolean locked) {
    }

    public int getCylinders() {
        return -1;
    }

    public int getHeads() {
        return -1;
    }

    public int getSectors() {
        return -1;
    }

    public Type getType() {
        return Type.FLOPPY;
    }

    public String toString() {
        return "Floppy: " + super.toString();
    }

}

