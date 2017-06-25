package com.zms.zpc.emulator;

import com.zms.zpc.emulator.processor.ProcessorConfig;

/**
 * Created by 张小美 on 17/五月/25.
 * Copyright 2002-2016
 */
public class PCConfig {

    private ProcessorConfig processorConfig;

    private long memoryChipLen;
    private int memoryCount;

    private String bios;

    public PCConfig() {
        setDefault();
    }

    private void setDefault() {
        this.processorConfig = new ProcessorConfig();

        this.memoryChipLen=512L*1024*1024;
        this.memoryCount=1;

        this.bios="BIOS-bochs-latest";
    }

    public static PCConfig defaultPCConfig() {
        return new PCConfig();
    }

    public ProcessorConfig getProcessorConfig() {
        return processorConfig;
    }

    public long getMemoryChipLen() {
        return memoryChipLen;
    }

    public void setMemoryChipLen(long memoryChipLen) {
        this.memoryChipLen = memoryChipLen;
    }

    public int getMemoryCount() {
        return memoryCount;
    }

    public void setMemoryCount(int memoryCount) {
        this.memoryCount = memoryCount;
    }

    public String getBios() {
        return bios;
    }

}
