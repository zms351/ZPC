package com.zms.zpc.emulator.store.impl;

import com.zms.zpc.emulator.store.*;

import java.io.IOException;
import java.util.logging.*;

/**
 * Created by 张小美 on 2019-06-09.
 * Copyright 2002-2016
 */
public abstract class RawBlockDevice implements BlockDevice {

    private static final Logger LOGGING = Logger.getLogger(RawBlockDevice.class.getName());

    private SeekableIODevice data;
    private long totalSectors;

    /**
     * Constructs an instance backed by the given <code>SeekableIODevice</code>.
     *
     * @param data device backing
     */
    protected RawBlockDevice(SeekableIODevice data) {
        setData(data);
    }

    public int read(long sectorNumber, byte[] buffer, int size) {
        Integer t;
        try {

            data.seek(sectorNumber * SECTOR_SIZE);
            int pos = 0;
            int toRead = Math.min(buffer.length, SECTOR_SIZE * size);
            while (true) {
                if (pos >= toRead)
                    return pos;
                int read = data.read(buffer, pos, toRead - pos);
                if (read < 0)
                    return pos;

                pos += read;
            }
        } catch (IOException e) {
            LOGGING.log(Level.WARNING, "error reading sector " + sectorNumber + ", size = " + size, e);
            return -1;
        }
    }

    public int write(long sectorNumber, byte[] buffer, int size) {
        try {
            data.seek(sectorNumber * SECTOR_SIZE);
            data.write(buffer, 0, size * SECTOR_SIZE);
        } catch (IOException e) {
            LOGGING.log(Level.WARNING, "error waiting", e);
            return -1;
        }
        return 0;
    }

    public long getTotalSectors() {
        return totalSectors;
    }

    public boolean isInserted() {
        return (data != null);
    }

    public boolean isReadOnly() {
        return data.readOnly();
    }

    public void close() {
        try {
            if (data != null)
                data.close();
        } catch (IOException e) {
            LOGGING.log(Level.INFO, "Couldn't close device", e);
        }
    }

    public void configure(String specs) throws IOException {
        data.configure(specs);
    }

    /**
     * Changes the backing for this device.
     *
     * @param data new backing device
     */
    protected final void setData(SeekableIODevice data) {
        this.data = data;
        if (data == null)
            totalSectors = 0;
        else
            totalSectors = data.length() / SECTOR_SIZE;
    }

    public String toString() {
        if (data == null)
            return "<empty>";
        else
            return data.toString();
    }

}

