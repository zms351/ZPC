package com.zms.zpc.emulator.store;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.board.helper.BaseDevice;
import com.zms.zpc.emulator.store.impl.FloppyBlockDevice;

/**
 * Created by 张小美 on 17/八月/13.
 * Copyright 2002-2016
 */
public class FloppyDrive extends BaseDevice {

    static final int MOTOR_ON = 0x01; // motor on/off

    static final int REVALIDATE = 0x02; // Revalidated

    static final int DOUBLE_SIDES = 0x01;

    public MotherBoard mb;
    public FloppyController.DriveType drive;

    BlockDevice device;

    int driveFlags;
    int perpendicular;
    int head;
    int headCount;
    int track;
    int sector;
    int sectorCount;
    int direction;
    int readWrite;
    int flags;
    int maxTrack;
    int bps;
    int readOnly;

    int driverIndex;
    FloppyFormat format;

    public FloppyDrive(MotherBoard mb, int index) {
        this.mb = mb;
        this.driverIndex = index;
        this.init();
    }

    protected void init() {
        drive = FloppyController.DriveType.DRIVE_144;
        if (driverIndex == 0) {
            //A驱
            String floppyA = mb.pc.getConfig().getFloppyA();
            if (floppyA != null && floppyA.length() > 0) {
                try {
                    SeekableIODevice d = mb.pc.getSeekableIODevice(this, "fda", floppyA, true, false);
                    assert d != null;
                    this.device = new FloppyBlockDevice(d);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
        }
        reset();
    }

    @Override
    public void write(int address, long v, int width) {

    }

    public int seek(int seekHead, int seekTrack, int seekSector, int enableSeek) {
        if ((seekTrack > maxTrack) || (seekHead != 0 && (headCount == 0)))
            return 2;

        if (seekSector > sectorCount)
            return 3;

        int fileSector = calculateSector(seekTrack, seekHead, headCount, seekSector, sectorCount);
        if (fileSector != currentSector()) {
            if (enableSeek == 0)
                return 4;

            head = seekHead;
            if (track != seekTrack) {
                track = seekTrack;
                sector = seekSector;
                return 1;
            }

            sector = seekSector;
        }
        return 0;
    }

    int read(int sector, byte[] buffer, int length) {
        return device.read(0xffffffffL & sector, buffer, length);
    }

    public void recalibrate() {
        head = 0;
        track = 0;
        sector = 1;
        direction = 1;
        readWrite = 0;
    }

    @Override
    public long read(int address, int width) {
        return 0;
    }

    @Override
    public void reset() {
        stop();
        recalibrate();
        revalidate();
    }

    public int currentSector() {
        return calculateSector(track, head, headCount, sector, sectorCount);
    }

    private int calculateSector(int track, int head, int headCount, int sector, int sectorCount) {
        return ((((0xff & track) * headCount) + (0xff & head)) * (0xff & sectorCount)) + (0xff & sector) - 1;
    }

    public void start() {
        driveFlags |= MOTOR_ON;
    }

    public void stop() {
        driveFlags &= ~MOTOR_ON;
    }

    public int write(int sector, byte[] buffer, int length) {
        return device.write(0xffffffffL & sector, buffer, length);
    }

    private void revalidate() {
        driveFlags &= ~REVALIDATE;
        if (device != null && device.isInserted()) {
            format = FloppyFormat.findFormat(device.getTotalSectors(), drive);
            headCount = format.heads();
            if (headCount == 1)
                flags &= ~DOUBLE_SIDES;
            else
                flags |= DOUBLE_SIDES;
            maxTrack = format.tracks();
            sectorCount = (byte) format.sectors();
            readOnly = device.isReadOnly() ? 0x1 : 0x0;
            drive = format.drive();
        } else {
            sectorCount = 0;
            maxTrack = 0;
            flags &= ~DOUBLE_SIDES;
        }
        driveFlags |= REVALIDATE;
    }

    public String toString() {
        return (device == null) ? "<none>" : format.toString();
    }

}
