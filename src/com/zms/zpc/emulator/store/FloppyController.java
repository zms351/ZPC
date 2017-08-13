package com.zms.zpc.emulator.store;

import com.zms.zpc.emulator.board.*;
import com.zms.zpc.emulator.board.helper.BaseIODevice;
import com.zms.zpc.emulator.debug.DummyDebugger;
import com.zms.zpc.support.NotImplException;

import java.time.Clock;
import java.util.logging.Level;

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

    public InterruptController irqDevice;
    public DMAController dma;

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
        drivers[1].drive = DriveType.DRIVE_NONE;
        for (int port : ioPortsRequested()) {
            mb.ios.register(port, this);
        }
        irqDevice=mb.pic;
        dma=mb.dma1;
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
        FloppyDrive drive = getCurrentDrive();

        /* Reset Mode */
        if ((state & CONTROL_RESET) != 0) {
            LOGGING.log(Level.WARNING, "cannot write data in reset state");
            return;
        }
        state &= ~CONTROL_SLEEP;
        if ((dataState & STATE_STATE) == STATE_STATUS) {
            LOGGING.log(Level.WARNING, "cannot write data in status mode");
            return;
        }
        /* Is it write command time? */
        if ((dataState & STATE_STATE) == STATE_DATA) {
            /* FIFO data write */
            fifo[dataOffset++] = (byte) data;
            if (dataOffset % SECTOR_LENGTH == (SECTOR_LENGTH - 1) || dataOffset == dataLength)
                drive.write(drive.currentSector(), fifo, SECTOR_LENGTH);

            /* Switch from transfer mode to status mode
         * then from status mode to command mode
	     */
            if ((dataState & STATE_STATE) == STATE_DATA)
                stopTransfer((byte) 0x20, (byte) 0x00, (byte) 0x00);
            return;
        }
        if (dataOffset == 0) {
            /* Command */
            switch (data & 0x5f) {
                case 0x46:
                case 0x4c:
                case 0x50:
                case 0x56:
                case 0x59:
                case 0x5d:
                    dataLength = 9;
                    enqueue(drive, data);
                    return;
                default:
                    break;
            }
            switch (data & 0x7f) {
                case 0x45:
                case 0x49:
                    dataLength = 9;
                    enqueue(drive, data);
                    return;
                default:
                    break;
            }
            switch (data) {
                case 0x03:
                case 0x0f:
                    dataLength = 3;
                    enqueue(drive, data);
                    return;
                case 0x04:
                case 0x07:
                case 0x12:
                case 0x33:
                case 0x4a:
                    dataLength = 2;
                    enqueue(drive, data);
                    return;
                case 0x08:
                    fifo[0] = (byte) (0x20 | (drive.head << 2) | currentDrive);
                    fifo[1] = (byte) drive.track;
                    setFIFO(2, false);
                    resetIRQ();
                    interruptStatus = 0xc0;
                    return;
                case 0x0e:
                    /* Drives position */
                    fifo[0] = (byte) getDrive(0).track;
                    fifo[1] = (byte) getDrive(1).track;
                    fifo[2] = 0;
                    fifo[3] = 0;
                    /* timers */
                    fifo[4] = timer0;
                    fifo[5] = dmaEnabled ? (byte) (timer1 << 1) : (byte) 0;
                    fifo[6] = (byte) drive.sectorCount;
                    fifo[7] = (byte) ((lock << 7) | (drive.perpendicular << 2));
                    fifo[8] = config;
                    fifo[9] = preCompensationTrack;
                    setFIFO(10, false);
                    return;
                case 0x10:
                    fifo[0] = CONTROLLER_VERSION;
                    setFIFO(1, true);
                    return;
                case 0x13:
                    dataLength = 4;
                    enqueue(drive, data);
                    return;
                case 0x14:
                    lock = 0;
                    fifo[0] = 0;
                    setFIFO(1, false);
                    return;
                case 0x17:
                case 0x8f:
                case 0xcf:
                    dataLength = 3;
                    enqueue(drive, data);
                    return;
                case 0x18:
                    fifo[0] = 0x41; /* Stepping 1 */
                    setFIFO(1, false);
                    return;
                case 0x2c:
                    fifo[0] = 0;
                    fifo[1] = 0;
                    fifo[2] = (byte) getDrive(0).track;
                    fifo[3] = (byte) getDrive(1).track;
                    fifo[4] = 0;
                    fifo[5] = 0;
                    fifo[6] = timer0;
                    fifo[7] = timer1;
                    fifo[8] = (byte) drive.sectorCount;
                    fifo[9] = (byte) ((lock << 7) | (drive.perpendicular << 2));
                    fifo[10] = config;
                    fifo[11] = preCompensationTrack;
                    fifo[12] = pwrd;
                    fifo[13] = 0;
                    fifo[14] = 0;
                    setFIFO(15, true);
                    return;
                case 0x42:
                    dataLength = 9;
                    enqueue(drive, data);
                    return;
                case 0x4c:
                    dataLength = 18;
                    enqueue(drive, data);
                    return;
                case 0x4d:
                case 0x8e:
                    dataLength = 6;
                    enqueue(drive, data);
                    return;
                case 0x94:
                    lock = 1;
                    fifo[0] = 0x10;
                    setFIFO(1, true);
                    return;
                case 0xcd:
                    dataLength = 11;
                    enqueue(drive, data);
                    return;
                default:
                    /* Unknown command */
                    unimplemented();
                    return;
            }
        }
        enqueue(drive, data);
    }

    private void unimplemented() {
        fifo[0] = (byte) 0x80;
        setFIFO(1, false);
    }

    private FloppyDrive getCurrentDrive() {
        return getDrive(currentDrive);
    }

    private void stopTransfer(byte status0, byte status1, byte status2) {
        FloppyDrive drive = getCurrentDrive();

        fifo[0] = (byte) (status0 | (drive.head << 2) | currentDrive);
        fifo[1] = status1;
        fifo[2] = status2;
        fifo[3] = (byte) drive.track;
        fifo[4] = (byte) drive.head;
        fifo[5] = (byte) drive.sector;
        fifo[6] = SECTOR_SIZE_CODE;
        dataDirection = DIRECTION_READ;
        if ((state & CONTROL_BUSY) != 0) {
            dma.releaseDmaRequest(DMA_CHANNEL & 3);
            state &= ~CONTROL_BUSY;
        }
        setFIFO(7, true);
    }

    private void setFIFO(int fifoLength, boolean doIRQ) {
        dataDirection = DIRECTION_READ;
        dataLength = fifoLength;
        dataOffset = 0;
        dataState = (dataState & ~STATE_STATE) | STATE_STATUS;
        if (doIRQ)
            raiseIRQ(0x00);
    }

    private void resetIRQ() {
        irqDevice.setIRQ(INTERRUPT_LEVEL, 0);
        state &= ~CONTROL_INTERRUPT;
    }

    private void raiseIRQ(int status) {
        if (~(state & CONTROL_INTERRUPT) != 0) {
            irqDevice.setIRQ(INTERRUPT_LEVEL, 1);
            state |= CONTROL_INTERRUPT;
        }
        interruptStatus = status;
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

    public DriveType getDriveType(int number) {
        if (drivers[number] == null) {
            return DriveType.DRIVE_NONE;
        }
        return drivers[number].drive;
    }

    public enum DriveType {DRIVE_144, DRIVE_288, DRIVE_120, DRIVE_NONE}

}
