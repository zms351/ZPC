package com.zms.zpc.emulator.board;

import com.zms.zpc.emulator.board.helper.InputDataQueue;
import com.zms.zpc.support.BaseObj;

/**
 * Created by 张小美 on 17/七月/23.
 * Copyright 2002-2016
 */
public class Keyboard extends BaseObj implements IODevice {

    public MotherBoard mb;

    public Keyboard(MotherBoard mb) {
        this.mb = mb;
        this.init();
    }

    /* Keyboard Controller Commands */
    private static final byte KBD_CCMD_READ_MODE = (byte) 0x20; /* Read mode bits */
    private static final byte KBD_CCMD_WRITE_MODE = (byte) 0x60; /* Write mode bits */
    private static final byte KBD_CCMD_GET_VERSION = (byte) 0xA1; /* Get controller version */
    private static final byte KBD_CCMD_MOUSE_DISABLE = (byte) 0xA7; /* Disable mouse interface */
    private static final byte KBD_CCMD_MOUSE_ENABLE = (byte) 0xA8; /* Enable mouse interface */
    private static final byte KBD_CCMD_TEST_MOUSE = (byte) 0xA9; /* Mouse interface test */
    private static final byte KBD_CCMD_SELF_TEST = (byte) 0xAA; /* Controller self test */
    private static final byte KBD_CCMD_KBD_TEST = (byte) 0xAB; /* Keyboard interface test */
    private static final byte KBD_CCMD_KBD_DISABLE = (byte) 0xAD; /* Keyboard interface disable */
    private static final byte KBD_CCMD_KBD_ENABLE = (byte) 0xAE; /* Keyboard interface enable */
    private static final byte KBD_CCMD_READ_INPORT = (byte) 0xC0; /* read input port */
    private static final byte KBD_CCMD_READ_OUTPORT = (byte) 0xD0; /* read output port */
    private static final byte KBD_CCMD_WRITE_OUTPORT = (byte) 0xD1; /* write output port */
    private static final byte KBD_CCMD_WRITE_OBUF = (byte) 0xD2;
    private static final byte KBD_CCMD_WRITE_AUX_OBUF = (byte) 0xD3; /* Write to output buffer as if initiated by the auxiliary device */
    private static final byte KBD_CCMD_WRITE_MOUSE = (byte) 0xD4; /* Write the following byte to the mouse */
    private static final byte KBD_CCMD_DISABLE_A20 = (byte) 0xDD; /* HP vectra only ? */
    private static final byte KBD_CCMD_ENABLE_A20 = (byte) 0xDF; /* HP vectra only ? */
    private static final byte KBD_CCMD_RESET = (byte) 0xFE;

    /* Keyboard Commands */
    private static final byte KBD_CMD_SET_LEDS = (byte) 0xED; /* Set keyboard leds */
    private static final byte KBD_CMD_ECHO = (byte) 0xEE;
    private static final byte KBD_CMD_GET_ID = (byte) 0xF2; /* get keyboard ID */
    private static final byte KBD_CMD_SET_RATE = (byte) 0xF3; /* Set typematic rate */
    private static final byte KBD_CMD_ENABLE = (byte) 0xF4; /* Enable scanning */
    private static final byte KBD_CMD_RESET_DISABLE = (byte) 0xF5; /* reset and disable scanning */
    private static final byte KBD_CMD_RESET_ENABLE = (byte) 0xF6; /* reset and enable scanning */
    private static final byte KBD_CMD_RESET = (byte) 0xFF; /* Reset */

    /* Status Register Bits */
    private static final byte KBD_STAT_OBF = (byte) 0x01; /* Keyboard output buffer full */
    private static final byte KBD_STAT_IBF = (byte) 0x02; /* Keyboard input buffer full */
    private static final byte KBD_STAT_SELFTEST = (byte) 0x04; /* Self test successful */
    private static final byte KBD_STAT_CMD = (byte) 0x08; /* Last write was a command write (0=data) */
    private static final byte KBD_STAT_UNLOCKED = (byte) 0x10; /* Zero if keyboard locked */
    private static final byte KBD_STAT_MOUSE_OBF = (byte) 0x20; /* Mouse output buffer full */
    private static final byte KBD_STAT_GTO = (byte) 0x40; /* General receive/xmit timeout */
    private static final byte KBD_STAT_PERR = (byte) 0x80; /* Parity error */

    /* Controller Mode Register Bits */
    private static final int KBD_MODE_KBD_INT = 0x01; /* Keyboard data generate IRQ1 */
    private static final int KBD_MODE_MOUSE_INT = 0x02; /* Mouse data generate IRQ12 */
    private static final int KBD_MODE_SYS = 0x04; /* The system flag (?) */
    private static final int KBD_MODE_NO_KEYLOCK = 0x08; /* The keylock doesn't affect the keyboard if set */
    private static final int KBD_MODE_DISABLE_KBD = 0x10; /* Disable keyboard interface */
    private static final int KBD_MODE_DISABLE_MOUSE = 0x20; /* Disable mouse interface */
    private static final int KBD_MODE_KCC = 0x40; /* Scan code conversion to PC format */
    private static final int KBD_MODE_RFU = 0x80;

    /* Keyboard Replies */
    private static final byte KBD_REPLY_POR = (byte) 0xAA; /* Power on reset */
    private static final byte KBD_REPLY_ACK = (byte) 0xFA; /* Command ACK */
    private static final byte KBD_REPLY_RESEND = (byte) 0xFE; /* Command NACK, send the cmd again */

    public static final int IOA = 0x60;
    public static final int IOB = 0x64;

    public int status;
    private int mode;
    private boolean keyboardScanEnabled;
    private byte commandWrite;
    private InputDataQueue queue;

    private void init() {
        IODevices ios = mb.ios;
        ios.register(IOA, this);
        ios.register(IOB, this);
        this.reset();
    }

    @Override
    public void write(int address, long v, int width) {
        if (address == IOB && width == 8) {
            onCommand((byte) v);
        }
        if (address == IOA && width == 8) {
            onWriteData((byte) v);
        }
        update();
    }

    private void onWriteData(byte command) {
        switch (command) {
            case KBD_CMD_RESET_DISABLE:
                reset();
                keyboardScanEnabled = false;
                queue.writeData(KBD_REPLY_ACK, (byte) 0);
                break;
            case KBD_CMD_RESET:
                reset();
                queue.writeData(KBD_REPLY_ACK, (byte) 0);
                queue.writeData(KBD_REPLY_POR, (byte) 0);
                break;
            case KBD_CMD_ENABLE:
                keyboardScanEnabled = true;
                queue.writeData(KBD_REPLY_ACK, (byte) 0);
                break;
        }
    }

    private void onCommand(byte command) {
        switch (command) {
            case KBD_CCMD_SELF_TEST:
                status = (byte) ((status & ~KBD_STAT_OBF) | KBD_STAT_SELFTEST);
                queue.writeData((byte) 0x55, (byte) 0);
                break;
            case KBD_CCMD_KBD_TEST:
                queue.writeData((byte) 0x00, (byte) 0);
                break;
            case KBD_CCMD_KBD_ENABLE:
                mode &= ~KBD_MODE_DISABLE_KBD;
                break;
            case KBD_CCMD_MOUSE_ENABLE:
                mode &= ~KBD_MODE_DISABLE_MOUSE;
                break;
            case KBD_CCMD_WRITE_MODE:
            case KBD_CCMD_WRITE_OBUF:
            case KBD_CCMD_WRITE_AUX_OBUF:
            case KBD_CCMD_WRITE_MOUSE:
            case KBD_CCMD_WRITE_OUTPORT:
                commandWrite = command;
                break;
        }
    }

    @Override
    public long read(int address, int width) {
        if (address == IOB) {
            if (width == 8) {
                return status;
            }
        }
        if (address == IOA) {
            if (width == 8) {
                if (queue.readData()) {
                    return queue.data_;
                }
            }
        }
        return 0;
    }

    public void reset() {
        status = (byte) (KBD_STAT_CMD | KBD_STAT_UNLOCKED);
        if (queue == null) {
            queue = new InputDataQueue();
        }
        mode = KBD_MODE_KBD_INT | KBD_MODE_MOUSE_INT;
    }

    public void update() {
        if (!queue.isEmpty()) {
            status |= KBD_STAT_OBF;
        } else {
            status &= (~KBD_STAT_OBF);
        }
    }

    public int getStatus() {
        return status;
    }

    public int getMode() {
        return mode;
    }

    public boolean isKeyboardScanEnabled() {
        return keyboardScanEnabled;
    }

    public byte getCommandWrite() {
        return commandWrite;
    }

    public InputDataQueue getQueue() {
        return queue;
    }

}
