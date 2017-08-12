package com.zms.zpc.emulator.board;

import com.zms.zpc.emulator.*;
import com.zms.zpc.emulator.debug.DummyDebugger;
import com.zms.zpc.support.BaseObj;

/**
 * Created by 张小美 on 17/八月/12.
 * Copyright 2002-2016
 */
public class VirtualClock extends BaseObj implements Runnable {

    public MotherBoard mb;

    public VirtualClock(MotherBoard mb) {
        this.mb = mb;
        Thread thread = new Thread(this, this.getClass().getName() + " working thread");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try {
            PC pc = mb.pc;
            SimpleInterruptController pic = mb.pic;
            long start = System.currentTimeMillis();
            double m = 0x001800b0 / 24.0 / 3600;
            PCState state;
            long c1 = 0;
            long c2;
            while ((state = pc.getState()) != null) {
                if (state == PCState.Running) {
                    c2 = Math.round((System.currentTimeMillis() - start) / m);
                    if (c2 > c1) {
                        pic.setIRQ(0,0);
                        pic.setIRQ(0,1);
                    }
                    c1 = c2;
                }
                Thread.sleep(20);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            DummyDebugger.getInstance().onMessage(WARN, "%s exited!\n", Thread.currentThread().getName());
        }
    }

}
