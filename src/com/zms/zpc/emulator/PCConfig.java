package com.zms.zpc.emulator;

import com.zms.zpc.emulator.processor.ProcessorConfig;

/**
 * Created by 张小美 on 17/五月/25.
 * Copyright 2002-2016
 */
public class PCConfig {

    private ProcessorConfig processorConfig;

    public PCConfig() {
        this.processorConfig = new ProcessorConfig();
    }

    public static PCConfig defaultPCConfig() {
        return new PCConfig();
    }

    public ProcessorConfig getProcessorConfig() {
        return processorConfig;
    }

}
