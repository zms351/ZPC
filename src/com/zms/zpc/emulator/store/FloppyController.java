package com.zms.zpc.emulator.store;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.board.helper.BaseIODevice;
import com.zms.zpc.emulator.debug.DummyDebugger;
import com.zms.zpc.support.NotImplException;

import java.time.Clock;

/**
 * Created by 张小美 on 17/八月/13.
 * Copyright 2002-2016
 */
public class FloppyController extends BaseIODevice {

    public MotherBoard mb;
    private FloppyDrive[] drivers;

    private static final DummyDebugger LOGGING = DummyDebugger.getInstance();

    /* Will always be a fixed parameter for us */
    private static final int SECTOR_LENGTH = 512;
    private static final int SECTOR_SIZE_CODE = 2; // Sector size code

    /* Floppy disk drive emulation */
    private static final int CONTROL_ACTIVE = 0x01; /* XXX: suppress that */

    private static final int CONTROL_RESET = 0x02;
    private static final int CONTROL_SLEEP = 0x04; /* XXX: suppress that */

    private static final int CONTROL_BUSY = 0x08; /* dma transfer in progress */

    private static final int CONTROL_INTERRUPT = 0x10;
    private static final int DIRECTION_WRITE = 0;
    private static final int DIRECTION_READ = 1;
    private static final int DIRECTION_SCANE = 2;
    private static final int DIRECTION_SCANL = 3;
    private static final int DIRECTION_SCANH = 4;
    private static final int STATE_COMMAND = 0x00;
    private static final int STATE_STATUS = 0x01;
    private static final int STATE_DATA = 0x02;
    private static final int STATE_STATE = 0x03;
    private static final int STATE_MULTI = 0x10;
    private static final int STATE_SEEK = 0x20;
    private static final int STATE_FORMAT = 0x40;
    private static final byte CONTROLLER_VERSION = (byte) 0x90; /* Intel 82078 Controller */

    private static final int INTERRUPT_LEVEL = 6;
    private static final int DMA_CHANNEL = 2;
    private static final int IOPORT_BASE = 0x3f0;
    private boolean drivesUpdated;
    //private Timer resultTimer;
    private Clock clock;
    private int state;
    private boolean dmaEnabled;
    private int currentDrive;
    private int bootSelect;

    /* Command FIFO */
    private byte[] fifo;
    private int dataOffset;
    private int dataLength;
    private int dataState;
    private int dataDirection;
    private int interruptStatus;
    private byte eot; // last wanted sector

    /* State kept only to be returned back */
        /* Timers state */
    private byte timer0;
    private byte timer1;
    /* precompensation */
    private byte preCompensationTrack;
    private byte config;
    private byte lock;
    /* Power down config */
    private byte pwrd;

    public FloppyController(MotherBoard mb) {
        this.mb = mb;
        this.init();
    }

    public int[] ioPortsRequested() {
        return new int[]{IOPORT_BASE + 1, IOPORT_BASE + 2, IOPORT_BASE + 3,
                IOPORT_BASE + 4, IOPORT_BASE + 5, IOPORT_BASE + 7};
    }

    protected void init() {
        drivers = new FloppyDrive[2];
        drivers[0] = new FloppyDrive(mb);
        drivers[1] = new FloppyDrive(mb);
        drivers[1].driver = DriverType.DRIVE_NONE;
        for (int port : ioPortsRequested()) {
            mb.ios.register(port, this);
        }
        reset();
    }

    public int ioPortRead8(int address) {
        switch (address & 0x07) {
            case 0x01:
                return readStatusB();
            case 0x02:
                return readDOR();
            case 0x03:
                return readTape();
            case 0x04:
                return readMainStatus();
            case 0x05:
                return readData();
            case 0x07:
                return readDirection();
            default:
                return 0xff;
        }
    }

    private int readData() {
        throw new NotImplException();
    }

    private int readDirection() {
        int retval = 0;
        if (((getDrive(0).driveFlags & FloppyDrive.REVALIDATE) != 0) || ((getDrive(1).driveFlags & FloppyDrive.REVALIDATE) != 0))
            retval |= 0x80;

        getDrive(0).driveFlags &= ~FloppyDrive.REVALIDATE;
        getDrive(1).driveFlags &= ~FloppyDrive.REVALIDATE;

        return retval;
    }

    public int ioPortRead16(int address) {
        return (ioPortRead8(address) & 0xff) | ((ioPortRead8(address + 1) << 8) & 0xff00);
    }

    public int ioPortRead32(int address) {
        return (ioPortRead16(address) & 0xffff) | ((ioPortRead16(address + 2) << 16) & 0xffff0000);
    }

    public void ioPortWrite8(int address, int data) {
        switch (address & 0x07) {
            case 0x02:
                writeDOR(data);
                break;
            case 0x03:
                writeTape(data);
                break;
            case 0x04:
                writeRate(data);
                break;
            case 0x05:
                writeData(data);
                break;
            default:
                break;
        }
    }

    private void writeRate(int data) {
            /* Reset mode */
        if ((state & CONTROL_RESET) != 0)
            return;

            /* Reset: autoclear */
        if ((data & 0x80) != 0) {
            state |= CONTROL_RESET;
            reset(true);
            state &= ~CONTROL_RESET;
        }
        if ((data & 0x40) != 0) {
            state |= CONTROL_SLEEP;
            reset(true);
        }
        //precomp = (data >>> 2) & 0x07;
    }

    private void writeTape(int data) {
            /* Reset mode */
        if ((state & CONTROL_RESET) != 0)
            return;

            /* Disk boot selection indicator */
        bootSelect = (data >>> 2) & 1;
        /* Tape indicators: never allow */
    }

    private void writeData(int data) {
        throw new NotImplException();
    }

    private void writeDOR(int data) {
            /* Reset mode */
        if (((state & CONTROL_RESET) != 0) && ((data & 0x04) == 0))
            return;

            /* Drive motors state indicators */
        if ((data & 0x20) != 0)
            getDrive(1).start();
        else
            getDrive(1).stop();

        if ((data & 0x10) != 0)
            getDrive(0).start();
        else
            getDrive(0).stop();
            /* DMA enable */

            /* Reset */
        if ((data & 0x04) == 0)
            if ((state & CONTROL_RESET) == 0)
                state |= CONTROL_RESET;
            else if ((state & CONTROL_RESET) != 0) {
                reset(true);
                state &= ~(CONTROL_RESET | CONTROL_SLEEP);
            }
            /* Selected drive */
        currentDrive = data & 1;
    }

    public void ioPortWrite16(int address, int data) {
        ioPortWrite8(address, data & 0xff);
        ioPortWrite8(address + 1, (data >>> 8) & 0xff);
    }

    public void ioPortWrite32(int address, int data) {
        ioPortWrite16(address, data & 0xffff);
        ioPortWrite16(address + 2, (data >>> 16) & 0xffff);
    }

    private int readStatusB() {
        return 0;
    }

    private int readDOR() {
        int retval = 0;

            /* Drive motors state indicators */
        if ((getDrive(0).driveFlags & FloppyDrive.MOTOR_ON) != 0)
            retval |= 1 << 5;
        if ((getDrive(1).driveFlags & FloppyDrive.MOTOR_ON) != 0)
            retval |= 1 << 4;
            /* DMA enable */
        retval |= dmaEnabled ? 1 << 3 : 0;
            /* Reset indicator */
        retval |= (state & CONTROL_RESET) == 0 ? 1 << 2 : 0;
            /* Selected drive */
        retval |= currentDrive;

        return retval;
    }

    private int readTape() {
            /* Disk boot selection indicator */
        return bootSelect << 2;
        /* Tape indicators: never allowed */
    }

    private int readMainStatus() {
        int retval = 0;

        state &= ~(CONTROL_SLEEP | CONTROL_RESET);
        if ((state & CONTROL_BUSY) == 0) {
                /* Data transfer allowed */
            retval |= 0x80;
                /* Data transfer direction indicator */
            if (dataDirection == DIRECTION_READ)
                retval |= 0x40;
        }
            /* Should handle 0x20 for SPECIFY command */
            /* Command busy indicator */
        if ((dataState & STATE_STATE) == STATE_DATA || (dataState & STATE_STATE) == STATE_STATUS)
            retval |= 0x10;

        return retval;
    }

    private FloppyDrive getDrive(int driveNumber) {
        return drivers[driveNumber - bootSelect];
    }

    private void enqueue(FloppyDrive drive, int data) {
        throw new NotImplException();
    }

    @Override
    public void reset() {

    }

    private void reset(boolean doIRQ) {
    }

    public DriverType getDriveType(int number) {
        if (drivers[number] == null) {
            return DriverType.DRIVE_NONE;
        }
        return drivers[number].driver;
    }

    public enum DriverType {DRIVE_144, DRIVE_288, DRIVE_120, DRIVE_NONE}

}
