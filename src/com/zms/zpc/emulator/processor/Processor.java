package com.zms.zpc.emulator.processor;

/**
 * Created by 张小美 on 17/五月/24.
 * Copyright 2002-2016
 */
public class Processor {

    private ProcessorConfig config;

    public Regs regs;

    public Processor(ProcessorConfig config) {
        assert config != null;
        this.config = config;
        init();
    }

    private void init() {
        regs = new Regs(this);
    }

    public ProcessorConfig getConfig() {
        return config;
    }

    public Regs getRegs() {
        return regs;
    }

    public CPUMode getMode() {
        return regs.bits.getMode();
    }

}
