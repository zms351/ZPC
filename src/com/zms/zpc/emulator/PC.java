package com.zms.zpc.emulator;

import com.zms.zpc.emulator.hardware.*;
import com.zms.zpc.emulator.processor.*;
import com.zms.zpc.emulator.processor.reg.Segment;
import com.zms.zpc.execute.*;

/**
 * Created by 张小美 on 17/五月/25.
 * Copyright 2002-2016
 */
public class PC implements Runnable {

    public Processor processor, cpu;
    private PCConfig config;
    private PCState state = PCState.Shutddown;
    public RAM memory;

    public PC() {
        this(null);
    }

    public PC(PCConfig config) {
        if (config == null) {
            config = PCConfig.defaultPCConfig();
        }
        this.config = config;
        this.init();
    }

    private void init() {
        this.processor = new Processor(config.getProcessorConfig());
        this.cpu = this.processor;
        this.memory = new PhysicalMemory(config.getMemoryChipLen(), config.getMemoryCount());
    }

    public PCConfig getConfig() {
        return config;
    }

    public Processor getProcessor() {
        return processor;
    }

    public PCState getState() {
        return state;
    }

    public void setState(PCState state) {
        this.state = state;
    }

    public void powerOn() {
        synchronized (this) {
            if (state == PCState.Shutddown) {
                state = PCState.Reset;
                Thread thread = new Thread(this);
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    public void powerOff() {
        synchronized (this) {
            if (state == PCState.Running) {
                state = PCState.Shutddown;
            }
        }
    }

    public void reset() {
        synchronized (this) {
            if (state == PCState.Running) {
                state = PCState.Reset;
            }
        }
    }

    private void doReset() {
        synchronized (this) {
            Segment cs = cpu.regs.cs;
            cs.setValue16(0xf000);
            //cs.setBase(0xffff0000);
            cpu.regs.eip.setValue32(0xFFF0);
            cpu.regs.bits.pe.clear();
            memory=new RealModeMemory(memory);
            state = PCState.Running;
        }
    }

    @Override
    public void run() {
        try {
            CodeExecutor executor=new CodeExecutor();
            CodeInputStream input=new CodeInputStream();
            while (state != PCState.Shutddown) {
                if (state == PCState.Reset) {
                    doReset();
                    continue;
                }
                if (state == PCState.Running) {
                    input.seek(this);
                    executor.execute(this,input);
                } else {
                    Thread.sleep(100);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
