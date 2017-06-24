package com.zms.zpc.emulator;

import com.zms.zpc.emulator.processor.Processor;

/**
 * Created by 张小美 on 17/五月/25.
 * Copyright 2002-2016
 */
public class PC implements Runnable {

    private Processor processor, cpu;
    private PCConfig config;
    private PCState state = PCState.Shutddown;
    private Memory memory;

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
        this.memory = new Memory(config.getMemoryChipLen(), config.getMemoryCount());
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

    public Processor getCpu() {
        return cpu;
    }

    public Memory getMemory() {
        return memory;
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
            cpu.regs.cs.setValue16(0xf000);
            state = PCState.Running;
        }
    }

    @Override
    public void run() {
        while (state != PCState.Shutddown) {
            if (state == PCState.Reset) {
                doReset();
                continue;
            }
            if (state == PCState.Running) {
                //todo
            }
        }
    }

}
