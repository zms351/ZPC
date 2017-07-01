package com.zms.zpc.emulator;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.debug.*;
import com.zms.zpc.emulator.memory.*;
import com.zms.zpc.emulator.processor.Processor;
import com.zms.zpc.emulator.reg.Segment;
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
    public MotherBoard board;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
        this.board = new MotherBoard(this);
        this.setName(config.getName());
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
    private Object pauseObj;
    private IDebugger _debugger;

    public IDebugger getDebugger() {
        if (_debugger == null) {
            _debugger = new DummyDebugger();
        }
        return _debugger;
    }

    public void setDebugger(IDebugger debugger) {
        this._debugger = debugger;
    }

    public void setPause(int pauseCommand, Object pauseObj) {
        if (this.pauseCommand == 0) {
            this.pauseObj = pauseObj;
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
                Thread thread = new Thread(this,getName()+"执行线程");
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    public void powerOff() {
        synchronized (this) {
            if (state == PCState.Running || state == PCState.Pause) {
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
            cs.setValue16(0xf000, true);
            //cs.base.setValue64(0xffff0000L);
            cpu.regs.rip.setValue64(0xFFF0);
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
            CodeStream stream = new CodeStream();
            while (state != PCState.Shutddown) {
                if (state == PCState.Reset) {
                    doReset();
                    continue;
                }
                if (state == PCState.Running) {
                    stream.seek(this);
                    executor.execute(this, stream);
                } else if (state == PCState.Pause) {
                    int command = pauseCommand;
                    pauseCommand = 0;
                    switch (command) {
                        case 11: {     //step into
                            stream.seek(this);
                            executor.execute(this, stream);
                            break;
                        }
                        case 12: {  //decompile
                            stream.seek(this);
                            String code = executor.decode(this, stream);
                            getDebugger().onMessage(12, code);
                            break;
                        }
                        case 13: {  //replace instruction
                            byte[] bytes= (byte[]) this.pauseObj;
                            if(bytes.length>0) {
                                stream.seek(this);
                                stream.write(bytes);
                            }
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
            System.out.println(Thread.currentThread().getName() + " exited!");
        }
    }

}
