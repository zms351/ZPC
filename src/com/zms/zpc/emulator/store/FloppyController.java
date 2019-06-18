package com.zms.zpc.emulator.store;

import com.zms.zpc.emulator.board.*;
import com.zms.zpc.emulator.board.helper.*;
import com.zms.zpc.emulator.board.time.TimerResponsive;
import com.zms.zpc.emulator.debug.DummyDebugger;
import com.zms.zpc.support.NotImplException;

import java.time.Clock;
import java.util.logging.Level;

/**
 * Created by 张小美 on 17/八月/13.
 * Copyright 2002-2016
 */
public class FloppyController extends BaseIODevice implements DMATransferCapable, TimerResponsive {

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
        drivers[0] = new FloppyDrive(mb, 0);
        drivers[1] = new FloppyDrive(mb, 1);
        drivers[1].drive = DriveType.DRIVE_NONE;
        for (int port : ioPortsRequested()) {
            mb.ios.register(port, this);
        }
        irqDevice = mb.pic;
        dma = mb.dma1;
        fifo = new byte[SECTOR_LENGTH];
        reset();
    }

    public int getType() {
        return 1;
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
        FloppyDrive drive;

        drive = getCurrentDrive();
        state &= ~CONTROL_SLEEP;
        if ((dataState & STATE_STATE) == STATE_COMMAND) {
            LOGGING.log(Level.WARNING, "cannot read data in command state");
            return 0;
        }

        int offset = dataOffset;
        if ((dataState & STATE_STATE) == STATE_DATA) {
            offset %= SECTOR_LENGTH;
            if (offset == 0) {
                int length = Math.min(dataLength - dataOffset, SECTOR_LENGTH);
                drive.read(drive.currentSector(), fifo, length);
            }
        }
        int retval = fifo[offset];
        if (++dataOffset == dataLength) {
            dataOffset = 0;
            /* Switch from transfer mode to status mode
             * then from status mode to command mode
             */
            if ((dataState & STATE_STATE) == STATE_DATA)
                stopTransfer((byte) 0x20, (byte) 0x00, (byte) 0x00);
            else {
                resetFIFO();
                resetIRQ();
            }
        }

        return retval;
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
        fifo[dataOffset] = (byte) data;
        if (++dataOffset == dataLength) {
            if ((dataState & STATE_FORMAT) != 0) {
                formatSector();
                return;
            }
            switch (fifo[0] & 0x1f) {
                case 0x06:
                    startTransfer(DIRECTION_READ);
                    return;
                case 0x0c:
                    startTransferDelete(DIRECTION_READ);
                    return;
                case 0x16:
                    stopTransfer((byte) 0x20, (byte) 0x00, (byte) 0x00);
                    return;
                case 0x10:
                    startTransfer(DIRECTION_SCANE);
                    return;
                case 0x19:
                    startTransfer(DIRECTION_SCANL);
                    return;
                case 0x1d:
                    startTransfer(DIRECTION_SCANH);
                    return;
                default:
                    break;
            }
            switch (fifo[0] & 0x3f) {
                case 0x05:
                    startTransfer(DIRECTION_WRITE);
                    return;
                case 0x09:
                    startTransferDelete(DIRECTION_WRITE);
                    return;
                default:
                    break;
            }
            switch (fifo[0]) {
                case 0x03:
                    timer0 = (byte) ((fifo[1] >>> 4) & 0xf);
                    timer1 = (byte) (fifo[2] >>> 1);
                    dmaEnabled = ((fifo[2] & 1) != 1);
                    resetFIFO();
                    break;
                case 0x04:
                    currentDrive = fifo[1] & 1;
                    drive = getCurrentDrive();
                    drive.head = ((fifo[1] >>> 2) & 1);
                    fifo[0] = (byte) ((drive.readOnly << 6) | (drive.track == 0 ? 0x10 : 0x00) | (drive.head << 2) | currentDrive | 0x28);
                    setFIFO(1, false);
                    break;
                case 0x07:
                    currentDrive = fifo[1] & 1;
                    drive = getCurrentDrive();
                    drive.recalibrate();
                    resetFIFO();
                    raiseIRQ(0x20);
                    break;
                case 0x0f:
                    currentDrive = fifo[1] & 1;
                    drive = getCurrentDrive();
                    drive.start();
                    if (fifo[2] <= drive.track)
                        drive.direction = 1;
                    else
                        drive.direction = 0;
                    resetFIFO();
                    if (fifo[2] > drive.maxTrack)
                        raiseIRQ(0x60);
                    else {
                        drive.track = fifo[2];
                        raiseIRQ(0x20);
                    }
                    break;
                case 0x12:
                    if ((fifo[1] & 0x80) != 0)
                        drive.perpendicular = fifo[1] & 0x7;
                    /* No result back */
                    resetFIFO();
                    break;
                case 0x13:
                    config = fifo[2];
                    preCompensationTrack = fifo[3];
                    /* No result back */
                    resetFIFO();
                    break;
                case 0x17:
                    pwrd = fifo[1];
                    fifo[0] = fifo[1];
                    setFIFO(1, true);
                    break;
                case 0x33:
                    /* No result back */
                    resetFIFO();
                    break;
                case 0x42:
                    LOGGING.log(Level.INFO, "implement READ_TRACK command");
                    startTransfer(DIRECTION_READ);
                    break;
                case 0x4A:
                    /* XXX: should set main status register to busy */
                    drive.head = (fifo[1] >>> 2) & 1;
                    //resultTimer.setExpiry(clock.getEmulatedNanos() + (clock.getTickRate() / 50));
                    if (drive.head >= Integer.MIN_VALUE) {
                        throw new NotImplException();
                    }
                    break;
                case 0x4C:
                    /* RESTORE */
                    /* Drives position */
                    getDrive(0).track = fifo[3];
                    getDrive(1).track = fifo[4];
                    /* timers */
                    timer0 = fifo[7];
                    timer1 = fifo[8];
                    drive.sectorCount = fifo[9];
                    lock = (byte) (fifo[10] >>> 7);
                    drive.perpendicular = (fifo[10] >>> 2) & 0xf;
                    config = fifo[11];
                    preCompensationTrack = fifo[12];
                    pwrd = fifo[13];
                    resetFIFO();
                    break;
                case 0x4D:
                    /* FORMAT_TRACK */
                    currentDrive = fifo[1] & 1;
                    drive = getCurrentDrive();
                    dataState |= STATE_FORMAT;
                    if ((fifo[0] & 0x80) != 0)
                        dataState |= STATE_MULTI;
                    else
                        dataState &= ~STATE_MULTI;
                    dataState &= ~STATE_SEEK;
                    drive.bps = fifo[2] > 7 ? 0x4000 : (0x80 << fifo[2]);
                    drive.sectorCount = fifo[3];

                    /* Bochs BIOS is buggy and don't send format informations
                     * for each sector. So, pretend all's done right now...
                     */
                    dataState &= ~STATE_FORMAT;
                    stopTransfer((byte) 0x00, (byte) 0x00, (byte) 0x00);
                    break;
                case (byte) 0x8E:
                    /* DRIVE_SPECIFICATION_COMMAND */
                    if ((fifo[dataOffset - 1] & 0x80) != 0)
                        /* Command parameters done */
                        if ((fifo[dataOffset - 1] & 0x40) != 0) {
                            fifo[0] = fifo[1];
                            fifo[2] = 0;
                            fifo[3] = 0;
                            setFIFO(4, true);
                        } else
                            resetFIFO();
                    else if (dataLength > 7) {
                        /* ERROR */
                        fifo[0] = (byte) (0x80 | (drive.head << 2) | currentDrive);
                        setFIFO(1, true);
                    }
                    break;
                case (byte) 0x8F:
                    /* RELATIVE_SEEK_OUT */
                    currentDrive = fifo[1] & 1;
                    drive = getCurrentDrive();
                    drive.start();
                    drive.direction = 0;
                    if (fifo[2] + drive.track >= drive.maxTrack)
                        drive.track = drive.maxTrack - 1;
                    else
                        drive.track += fifo[2];
                    resetFIFO();
                    raiseIRQ(0x20);
                    break;
                case (byte) 0xCD:
                    /* FORMAT_AND_WRITE */
                    LOGGING.log(Level.INFO, "implement FORMAT_AND_WRITE command");
                    unimplemented();
                    break;
                case (byte) 0xCF:
                    /* RELATIVE_SEEK_IN */
                    currentDrive = fifo[1] & 1;
                    drive = getCurrentDrive();
                    drive.start();
                    drive.direction = 1;
                    if (fifo[2] > drive.track)
                        drive.track = 0;
                    else
                        drive.track -= fifo[2];
                    resetFIFO();
                    /* Raise Interrupt */
                    raiseIRQ(0x20);
                    break;
            }
        }
    }

    private void formatSector() {
        LOGGING.log(Level.INFO, "format sector not implemented");
    }

    private static int memcmp(byte[] a1, byte[] a2, int offset, int length) {
        for (int i = 0; i < length; i++)
            if (a1[i] != a2[i + offset])
                return a1[i] - a2[i + offset];
        return 0;
    }

    public int handleTransfer(DMAController.DMAChannel channel, int pos, int size) {
        byte status0 = 0x00, status1 = 0x00, status2 = 0x00;

        if ((state & CONTROL_BUSY) == 0)
            return 0;

        FloppyDrive drive = getCurrentDrive();

        if ((dataDirection == DIRECTION_SCANE) || (dataDirection == DIRECTION_SCANL) || (dataDirection == DIRECTION_SCANH))
            status2 = 0x04;
        size = Math.min(size, dataLength);
        if (drive.device == null) {
            if (dataDirection == DIRECTION_WRITE)
                stopTransfer((byte) 0x60, (byte) 0x00, (byte) 0x00);
            else
                stopTransfer((byte) 0x40, (byte) 0x00, (byte) 0x00);
            return 0;
        }

        int relativeOffset = dataOffset % SECTOR_LENGTH;
        int startOffset;
        for (startOffset = dataOffset; dataOffset < size; ) {
            int length = Math.min(size - dataOffset, SECTOR_LENGTH - relativeOffset);
            if ((dataDirection != DIRECTION_WRITE) || (length < SECTOR_LENGTH) || (relativeOffset != 0))
                /* READ & SCAN commands and realign to a sector for WRITE */
                if (drive.read(drive.currentSector(), fifo, 1) < 0)
                    /* Sure, image size is too small... */
                    for (int i = 0; i < Math.min(fifo.length, SECTOR_LENGTH); i++)
                        fifo[i] = (byte) 0x00;
            switch (dataDirection) {
                case DIRECTION_READ:
                    /* READ commands */
                    channel.writeMemory(fifo, relativeOffset, dataOffset, length);
                    break;
                case DIRECTION_WRITE:
                    /* WRITE commands */
                    channel.readMemory(fifo, relativeOffset, dataOffset, length);
                    if (drive.write(drive.currentSector(), fifo, 1) < 0) {
                        stopTransfer((byte) 0x60, (byte) 0x00, (byte) 0x00);
                        return length;
                    }
                    break;
                default:
                    /* SCAN commands */
                {
                    byte[] tempBuffer = new byte[SECTOR_LENGTH];
                    channel.readMemory(tempBuffer, 0, dataOffset, length);
                    int ret = memcmp(tempBuffer, fifo, relativeOffset, length);
                    if (ret == 0) {
                        status2 = 0x08;
                        length = dataOffset - startOffset;
                        if (dataDirection == DIRECTION_SCANE || dataDirection == DIRECTION_SCANL || dataDirection == DIRECTION_SCANH)
                            status2 = 0x08;
                        if ((dataState & STATE_SEEK) != 0)
                            status0 |= 0x20;
                        dataLength -= length;
                        //    if (dataLength == 0)
                        stopTransfer(status0, status1, status2);
                        return length;

                    }
                    if ((ret < 0 && dataDirection == DIRECTION_SCANL) || (ret > 0 && dataDirection == DIRECTION_SCANH)) {
                        status2 = 0x00;

                        length = dataOffset - startOffset;
                        if (dataDirection == DIRECTION_SCANE || dataDirection == DIRECTION_SCANL || dataDirection == DIRECTION_SCANH)
                            status2 = 0x08;
                        if ((dataState & STATE_SEEK) != 0)
                            status0 |= 0x20;
                        dataLength -= length;
                        //    if (dataLength == 0)
                        stopTransfer(status0, status1, status2);

                        return length;
                    }
                }
                break;
            }
            dataOffset += length;
            relativeOffset = dataOffset % SECTOR_LENGTH;
            if (relativeOffset == 0)
                /* Seek to next sector */
                    /* XXX: drive.sect >= drive.last_sect should be an
    		   error in fact */
                if ((drive.sector >= drive.sectorCount) || (drive.sector == eot)) {
                    drive.sector = 1;
                    if ((dataState & STATE_MULTI) != 0)
                        if ((drive.head == 0) && (drive.headCount > 0))
                            drive.head = 1;
                        else {
                            drive.head = 0;
                            drive.track++;
                        }
                    else
                        drive.track++;
                } else
                    drive.sector++;
        }

        int length = dataOffset - startOffset;
        if (dataDirection == DIRECTION_SCANE || dataDirection == DIRECTION_SCANL || dataDirection == DIRECTION_SCANH)
            status2 = 0x08;
        if ((dataState & STATE_SEEK) != 0)
            status0 |= 0x20;
        dataLength -= length;
        //    if (dataLength == 0)
        stopTransfer(status0, status1, status2);

        return length;
    }

    private void resetFIFO() {
        dataDirection = DIRECTION_WRITE;
        dataOffset = 0;
        dataState = (dataState & ~STATE_STATE) | STATE_COMMAND;
    }

    public void callback() {
        stopTransfer((byte) 0x00, (byte) 0x00, (byte) 0x00);
    }

    private void startTransfer(int direction) {
        currentDrive = fifo[1] & 1;
        FloppyDrive drive = getCurrentDrive();
        byte kt = fifo[2];
        byte kh = fifo[3];
        byte ks = fifo[4];
        boolean didSeek = false;
        switch (drive.seek(0xff & kh, 0xff & kt, 0xff & ks, drive.sectorCount)) {
            case 2:
                /* sect too big */
                stopTransfer((byte) 0x40, (byte) 0x00, (byte) 0x00);
                fifo[3] = kt;
                fifo[4] = kh;
                fifo[5] = ks;
                return;
            case 3:
                /* track too big */
                stopTransfer((byte) 0x40, (byte) 0x80, (byte) 0x00);
                fifo[3] = kt;
                fifo[4] = kh;
                fifo[5] = ks;
                return;
            case 4:
                /* No seek enabled */
                stopTransfer((byte) 0x40, (byte) 0x00, (byte) 0x00);
                fifo[3] = kt;
                fifo[4] = kh;
                fifo[5] = ks;
                return;
            case 1:
                didSeek = true;
                break;
            default:
                break;
        }

        dataDirection = direction;
        dataOffset = 0;
        dataState = (dataState & ~STATE_STATE) | STATE_DATA;

        if ((fifo[0] & 0x80) != 0)
            dataState |= STATE_MULTI;
        else
            dataState &= ~STATE_MULTI;
        if (didSeek)
            dataState |= STATE_SEEK;
        else
            dataState &= ~STATE_SEEK;
        if (fifo[5] == 0x00)
            dataLength = fifo[8];
        else {
            dataLength = 128 << fifo[5];
            int temp = drive.sectorCount - ks + 1;
            if ((fifo[0] & 0x80) != 0)
                temp += drive.sectorCount;
            dataLength *= temp;
        }
        eot = fifo[6];
        if (dmaEnabled) {
            int dmaMode = dma.getChannelMode(DMA_CHANNEL & 3);
            dmaMode = (dmaMode >>> 2) & 3;
            if (((direction == DIRECTION_SCANE || direction == DIRECTION_SCANL || direction == DIRECTION_SCANH) && dmaMode == 0) ||
                    (direction == DIRECTION_WRITE && dmaMode == 2) || (direction == DIRECTION_READ && dmaMode == 1)) {
                // No access is allowed until DMA transfer has completed
                state |= CONTROL_BUSY;
                // simulate a data transfer rate of a spinning platter at 300 rpm (each sector should take 200,000/sectorPerTrack micro seconds
                dma.holdDmaRequest(DMA_CHANNEL & 3);
                return;
            } else
                LOGGING.log(Level.INFO, "DMA mode %d, direction %d\n", dmaMode, direction);
        }
        /* IO based transfer: calculate len */
        raiseIRQ(0x00);
    }

    private void startTransferDelete(int direction) {
        stopTransfer((byte) 0x60, (byte) 0x00, (byte) 0x00);
    }

    @Override
    public void reset() {
        this.dmaEnabled = true;
        this.state = CONTROL_ACTIVE;
        for (FloppyDrive driver : drivers) {
            if (driver != null) {
                driver.reset();
            }
        }
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
