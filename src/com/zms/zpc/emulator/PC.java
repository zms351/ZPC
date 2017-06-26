package com.zms.zpc.emulator;

import com.zms.zpc.emulator.hardware.*;
import com.zms.zpc.emulator.processor.Processor;
import com.zms.zpc.emulator.processor.reg.Segment;
import com.zms.zpc.execute.*;
import com.zms.zpc.support.GarUtils;

import java.io.InputStream;

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

    private PCState resetBefore;
    private int pauseCommand;

    public void setPauseCommand(int pauseCommand) {
        if (this.pauseCommand == 0) {
            this.pauseCommand = pauseCommand;
        }
    }

    public void powerOn(boolean pause) {
        synchronized (this) {
            if (state == PCState.Shutddown) {
                if (pause) {
                    resetBefore = PCState.Pause;
                } else {
                    resetBefore = state;
                }
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
                resetBefore = state;
                state = PCState.Reset;
            }
        }
    }

    public void pause() {
        synchronized (this) {
            if (state == PCState.Running) {
                state = PCState.Pause;
            } else if (state == PCState.Shutddown) {
                powerOn(true);
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
            memory = new RealModeMemory(memory);
            installBios();
            if (resetBefore == PCState.Pause) {
                state = PCState.Pause;
            } else {
                state = PCState.Running;
            }
        }
    }

    private void installBios() {
        String url = "images/" + config.getBios();
        byte[] bytes;
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(url)) {
            bytes = GarUtils.readAll(input);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        assert bytes.length > 100;
        memory.write(0, 0x100000 - bytes.length, bytes, 0, bytes.length);
    }

    @Override
    public void run() {
        try {
            CodeExecutor executor = new CodeExecutor();
            CodeInputStream input = new CodeInputStream();
            while (state != PCState.Shutddown) {
                if (state == PCState.Reset) {
                    doReset();
                    continue;
                }
                if (state == PCState.Running) {
                    input.seek(this);
                    executor.execute(this, input);
                } else if (state == PCState.Pause) {
                    int command = pauseCommand;
                    pauseCommand = 0;
                    switch (command) {
                        case 11: {     //decompile
                            //todo
                            break;
                        }
                        default:
                            Thread.sleep(100);
                    }
                } else {
                    Thread.sleep(100);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            System.out.println("code execute exited");
        }
    }

}
