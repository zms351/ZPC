package com.zms.zpc.emulator.board;

/**
 * Created by 张小美 on 17/八月/12.
 * Copyright 2002-2016
 */
public class SimpleInterruptController extends InterruptController {

    public SimpleInterruptController(MotherBoard mb) {
        super(mb);
    }

    @Override
    public void reset() {
        super.reset();
        irq = -1;
    }

    private int irq;

    @Override
    public void setIRQ(int irqNumber, int level) {
        if (level == 0 && irqNumber == 0) {
            //irq = 8;
        }
    }

    @Override
    public boolean hasInterrupt() {
        return irq >= 0;
    }

    @Override
    public int cpuGetInterrupt() {
        return irq;
    }

    @Override
    public void clearInterrupt() {
        irq = -1;
    }

}
