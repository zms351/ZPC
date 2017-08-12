package com.zms.zpc.emulator.board;

import com.zms.zpc.emulator.board.helper.BaseDevice;
import com.zms.zpc.emulator.debug.DummyDebugger;
import com.zms.zpc.support.NotImplException;

import java.util.logging.Level;

/**
 * Created by 张小美 on 17/八月/12.
 * Copyright 2002-2016
 */
public class InterruptController extends BaseDevice {

    public MotherBoard mb;

    public InterruptController(MotherBoard mb) {
        this.mb = mb;
        this.init();
        this.reset();
    }

    protected void init() {
        master = new InterruptControllerElement(true);
        slave = new InterruptControllerElement(false);
        for (int address : ioPortsRequested()) {
            mb.ios.register(address, this);
        }
    }

    private volatile int interruptFlags;

    public void raiseInterrupt() {
        interruptFlags |= IFLAGS_HARDWARE_INTERRUPT;
    }

    public void clearInterrupt() {
        interruptFlags &= ~IFLAGS_HARDWARE_INTERRUPT;
    }

    public void requestReset() {
        interruptFlags |= IFLAGS_RESET_REQUEST;
    }

    private static final DummyDebugger LOGGING = DummyDebugger.getInstance();

    private InterruptControllerElement master;
    private InterruptControllerElement slave;


    public int getIRQ0Vector() {
        return master.irqBase;
    }

    public int getSpuriousVector() {
        return slave.irqBase + 7;
    }

    public int getSpuriousMasterVector() {
        return master.irqBase + 7;
    }

    public void triggerSpuriousInterrupt() {
        setIRQ(2, 0);
        setIRQ(2, 1);
    }

    public void triggerSpuriousMasterInterrupt() {
        raiseInterrupt();
    }

    public int getMasterIRR() {
        return master.interruptRequestRegister;
    }

    private void updateIRQ() {
        int slaveIRQ, masterIRQ;
       /* first look at slave irq */
        slaveIRQ = slave.getIRQ();
        if (slaveIRQ >= 0) {
           /* if irq request by slave pic, signal Master PIC */
            master.setIRQ(2, 1);
            master.setIRQ(2, 0);
        }
       /* look at requested IRQ */
        masterIRQ = master.getIRQ();
        if (masterIRQ >= 0) {
            raiseInterrupt();
        }
    }

    /**
     * Set interrupt number <code>irqNumber</code> to level <code>level</code>.
     *
     * @param irqNumber interrupt channel number.
     * @param level     requested level.
     */
    public void setIRQ(int irqNumber, int level) {
        switch (irqNumber >>> 3) {
            case 0: //master
                master.setIRQ(irqNumber & 7, level);
                this.updateIRQ();
                break;
            case 1: //slave
                slave.setIRQ(irqNumber & 7, level);
                this.updateIRQ();
                break;
            default:
        }
    }

    /**
     * Return the highest priority interrupt request currently awaiting service
     * on this interrupt controller.  This is called by the processor emulation
     * once its <code>raiseInterrupt</code> method has been called, to get the
     * correct interrupt vector value.
     *
     * @return highest priority interrupt vector.
     */
    public int cpuGetInterrupt() {
        int masterIRQ, slaveIRQ;

       /* read the irq from the PIC */

        masterIRQ = master.getIRQ();
        if (masterIRQ >= 0) {
            master.intAck(masterIRQ);
            if (masterIRQ == 2) {
                slaveIRQ = slave.getIRQ();
                if (slaveIRQ >= 0) {
                    slave.intAck(slaveIRQ);
                } else {
                   /* spurious IRQ on slave controller */
                    slaveIRQ = 7;
                }
                this.updateIRQ();
                return slave.irqBase + slaveIRQ;
                //masterIRQ = slaveIRQ + 8;
            } else {
                this.updateIRQ();
                return master.irqBase + masterIRQ;
            }
        } else {
           /* spurious IRQ on host controller */
            masterIRQ = 7;
            this.updateIRQ();
            return master.irqBase + masterIRQ;
        }
    }

    private class InterruptControllerElement {
        private int lastInterruptRequestRegister; //edge detection
        private int interruptRequestRegister;
        private int interruptMaskRegister;
        private int interruptServiceRegister;

        private int priorityAdd; // highest IRQ priority
        private int irqBase;
        private boolean readRegisterSelect;
        private boolean poll;
        private boolean specialMask;
        private int initState;
        private boolean fourByteInit;
        private int elcr; //(elcr) PIIX3 edge/level trigger selection
        private int elcrMask;

        private boolean specialFullyNestedMode;

        private boolean autoEOI;
        private boolean rotateOnAutoEOI;

        private int[] ioPorts;

        public InterruptControllerElement(boolean master) {
            if (master) {
                ioPorts = new int[]{0x20, 0x21, 0x4d0};
                elcrMask = 0xf8;
            } else {
                ioPorts = new int[]{0xa0, 0xa1, 0x4d1};
                elcrMask = 0xde;
            }
        }

        /* BEGIN IODevice Methods */
        public int[] ioPortsRequested() {
            return ioPorts;
        }

        public int ioPortRead(int address) {
            if (poll) {
                poll = false;
                return this.pollRead(address);
            }

            if ((address & 1) == 0) {
                if (readRegisterSelect) {
                    return interruptServiceRegister;
                }

                return interruptRequestRegister;
            }

            return interruptMaskRegister;
        }

        public int elcrRead() {
            return elcr;
        }

        public boolean ioPortWrite(int address, byte data) //t/f updateIRQ
        {
            int priority, command, irq;
            address &= 1;
            if (address == 0) {
                if (0 != (data & 0x10)) {
                   /* init */
                    this.reset();
                    clearInterrupt();

                    initState = 1;
                    fourByteInit = ((data & 1) != 0);
                    if (0 != (data & 0x02))
                        LOGGING.log(Level.INFO, "single mode not supported");
                    if (0 != (data & 0x08))
                        LOGGING.log(Level.INFO, "level sensitive irq not supported");
                } else if (0 != (data & 0x08)) {
                    if (0 != (data & 0x04))
                        poll = true;
                    if (0 != (data & 0x02))
                        readRegisterSelect = ((data & 0x01) != 0);
                    if (0 != (data & 0x40))
                        specialMask = (((data >>> 5) & 1) != 0);
                } else {
                    command = data >>> 5;
                    switch (command) {
                        case 0:
                        case 4:
                            rotateOnAutoEOI = ((command >>> 2) != 0);
                            break;
                        case 1: // end of interrupt
                        case 5:
                            priority = this.getPriority(interruptServiceRegister);
                            if (priority != 8) {
                                irq = (priority + priorityAdd) & 7;
                                interruptServiceRegister &= ~(1 << irq);
                                if (command == 5)
                                    priorityAdd = (irq + 1) & 7;
                                return true;
                            }
                            break;
                        case 3:
                            irq = data & 7;
                            interruptServiceRegister &= ~(1 << irq);
                            return true;
                        case 6:
                            priorityAdd = (data + 1) & 7;
                            return true;
                        case 7:
                            irq = data & 7;
                            interruptServiceRegister &= ~(1 << irq);
                            priorityAdd = (irq + 1) & 7;
                            return true;
                        default:
                           /* no operation */
                            break;
                    }
                }
            } else {
                switch (initState) {
                    case 0:
                       /* normal mode */
                        interruptMaskRegister = data;
                        return true;
                    case 1:
                        irqBase = data & 0xf8;
                        initState = 2;
                        break;
                    case 2:
                        if (fourByteInit) {
                            initState = 3;
                        } else {
                            initState = 0;
                        }
                        break;
                    case 3:
                        specialFullyNestedMode = (((data >>> 4) & 1) != 0);
                        autoEOI = (((data >>> 1) & 1) != 0);
                        initState = 0;
                        break;
                }
            }
            return false;
        }

        public void elcrWrite(int data) {
            elcr = data & elcrMask;
        }
       /* END IODevice Methods */

        private int pollRead(int address) {
            int ret = this.getIRQ();
            if (ret < 0) {
                InterruptController.this.updateIRQ();
                return 0x07;
            }

            if (0 != (address >>> 7)) {
                InterruptController.this.masterPollCode();
            }
            interruptRequestRegister &= ~(1 << ret);
            interruptServiceRegister &= ~(1 << ret);
            if (0 != (address >>> 7) || ret != 2)
                InterruptController.this.updateIRQ();
            return ret;
        }

        public void setIRQ(int irqNumber, int level) {

            int mask;
            mask = (1 << irqNumber);
            if (0 != (elcr & mask)) {
               /* level triggered */
                if (0 != level) {
                    interruptRequestRegister |= mask;
                    lastInterruptRequestRegister |= mask;
                } else {
                    interruptRequestRegister &= ~mask;
                    lastInterruptRequestRegister &= ~mask;
                }
            } else {
               /* edge triggered */
                if (0 != level) {
                    if ((lastInterruptRequestRegister & mask) == 0) {
                        interruptRequestRegister |= mask;
                    }
                    lastInterruptRequestRegister |= mask;
                } else {
                    lastInterruptRequestRegister &= ~mask;
                }
            }
        }

        private int getPriority(int mask) {
            if ((0xff & mask) == 0) {
                return 8;
            }
            int priority = 0;
            while ((mask & (1 << ((priority + priorityAdd) & 7))) == 0) {
                priority++;
            }
            return priority;
        }

        public int getIRQ() {
            int mask, currentPriority, priority;

            mask = interruptRequestRegister & ~interruptMaskRegister;
            priority = this.getPriority(mask);
            if (priority == 8) {
                return -1;
            }
           /* compute current priority. If special fully nested mode on
       the master, the IRQ coming from the slave is not taken into
       account for the priority computation. */
            mask = interruptServiceRegister;
            if (specialFullyNestedMode && this.isMaster()) {
                mask &= ~(1 << 2);
            }
            currentPriority = this.getPriority(mask);

            if (priority < currentPriority) {
               /* higher priority found: an irq should be generated */
                return (priority + priorityAdd) & 7;
            } else {
                return -1;
            }
        }

        private void intAck(int irqNumber) {
            if (autoEOI) {
                if (rotateOnAutoEOI)
                    priorityAdd = (irqNumber + 1) & 7;
            } else {
                interruptServiceRegister |= (1 << irqNumber);
            }
           /* We don't clear a level sensitive interrupt here */
            if (0 == (elcr & (1 << irqNumber)))
                interruptRequestRegister &= ~(1 << irqNumber);
        }

        private boolean isMaster() {
            return InterruptController.this.master == this;
        }

        private void reset() {
            //zero all variables except elcrMask
            lastInterruptRequestRegister = 0x0;
            interruptRequestRegister = 0x0;
            interruptMaskRegister = 0x0;
            interruptServiceRegister = 0x0;

            priorityAdd = 0;
            irqBase = 0x0;
            readRegisterSelect = false;
            poll = false;
            specialMask = false;
            autoEOI = false;
            rotateOnAutoEOI = false;

            specialFullyNestedMode = false;

            initState = 0;
            fourByteInit = false;

            elcr = 0x0; //(elcr) PIIX3 edge/level trigger selection
        }

        public String toString() {
            if (isMaster()) {
                return (InterruptController.this).toString() + ": [Master Element]";
            } else {
                return (InterruptController.this).toString() + ": [Slave  Element]";
            }
        }
    }

    /* BEGIN IODevice Defined Methods */
    public int[] ioPortsRequested() {
        int[] masterIOPorts = master.ioPortsRequested();
        int[] slaveIOPorts = slave.ioPortsRequested();

        int[] temp = new int[masterIOPorts.length + slaveIOPorts.length];
        System.arraycopy(masterIOPorts, 0, temp, 0, masterIOPorts.length);
        System.arraycopy(slaveIOPorts, 0, temp, masterIOPorts.length, slaveIOPorts.length);

        return temp;
    }

    public int ioPortRead8(int address) {
        switch (address) {
            case 0x20:
            case 0x21:
                return 0xff & master.ioPortRead(address);
            case 0xa0:
            case 0xa1:
                return 0xff & slave.ioPortRead(address);
            case 0x4d0:
                return 0xff & master.elcrRead();
            case 0x4d1:
                return 0xff & slave.elcrRead();
            default:
        }
        return 0;
    }

    public int ioPortRead16(int address) {
        return (0xff & ioPortRead8(address)) |
                (0xff00 & (ioPortRead8(address + 1) << 8));
    }

    public int ioPortRead32(int address) {
        return (0xffff & ioPortRead16(address)) |
                (0xffff0000 & (ioPortRead16(address + 2) << 16));
    }

    public void ioPortWrite8(int address, int data) {
        switch (address) {
            case 0x20:
            case 0x21:
                if (master.ioPortWrite(address, (byte) data))
                    this.updateIRQ();
                break;
            case 0xa0:
            case 0xa1:
                if (slave.ioPortWrite(address, (byte) data))
                    this.updateIRQ();
                break;
            case 0x4d0:
                master.elcrWrite(data);
                break;
            case 0x4d1:
                slave.elcrWrite(data);
                break;
            default:
        }
    }

    public void ioPortWrite16(int address, int data) {
        this.ioPortWrite8(address, data);
        this.ioPortWrite8(address + 1, data >>> 8);
    }

    public void ioPortWrite32(int address, int data) {
        this.ioPortWrite16(address, data);
        this.ioPortWrite16(address + 2, data >>> 16);
    }

   /* END IODevice Defined Methods */

    private void masterPollCode() {
        master.interruptServiceRegister &= ~(1 << 2);
        master.interruptRequestRegister &= ~(1 << 2);
    }


    public void reset() {
        master.reset();
        slave.reset();

        interruptFlags = 0;
    }

    public String toString() {
        return "Intel i8259 Programmable Interrupt Controller";
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

    public boolean hasInterrupt() {
        return (interruptFlags & IFLAGS_HARDWARE_INTERRUPT) != 0;
    }

}
