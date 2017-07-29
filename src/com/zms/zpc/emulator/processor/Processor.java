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

    private CPUMode mode = CPUMode.Real;

    public void setMode(CPUMode mode) {
        this.mode = mode;
    }

    public CPUMode getMode() {
        return mode;
    }

    public void checkState() {
        if (!regs.bits.pe.get()) {
            setMode(CPUMode.Real);
        } else {
            if (getMode() == CPUMode.Real) {
                setMode(CPUMode.Protected32);
            }
        }
    }

}
