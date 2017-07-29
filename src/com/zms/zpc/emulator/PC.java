package com.zms.zpc.emulator;

import com.zms.zpc.emulator.board.MotherBoard;
import com.zms.zpc.emulator.debug.*;
import com.zms.zpc.emulator.memory.*;
import com.zms.zpc.emulator.processor.*;
import com.zms.zpc.execute.*;
import com.zms.zpc.support.*;

import java.io.InputStream;

/**
 * Created by 张小美 on 17/五月/25.
 * Copyright 2002-2016
 */
public class PC extends BaseObj implements Runnable {

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
            _debugger = DummyDebugger.getInstance();
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
                Thread thread = new Thread(this, getName() + "执行线程");
                thread.setDaemon(true);
                thread.start();
            } else if (state == PCState.Pause) {
                state = PCState.Running;
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
            board.reset();
            cpu.regs.bits.pe.clear();
            cpu.setMode(CPUMode.Real);
            cpu.checkState();
            cpu.regs.rip.setValue64(0xFFF0);
            cpu.regs.cs.setValue(0xf000);
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

    public int[] intObj = new int[2];

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
                    try {
                        stream.seek(this);
                        executor.execute(this, stream);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        state = PCState.Pause;
                        pauseCommand = 0;
                    }
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
                            byte[] bytes = (byte[]) this.pauseObj;
                            if (bytes.length > 0) {
                                stream.seek(this);
                                stream.write(bytes);
                            }
                            break;
                        }
                        case 14: {  //step over
                            stream.seek(this);
                            executor.execute(this, stream);
                            if (executor.ins == Call) {
                                pauseCommand = 16;
                                intObj[0] = 1;
                            }
                            break;
                        }
                        case 15: { //step out
                            stream.seek(this);
                            executor.execute(this, stream);
                            if (executor.ins != Ret) {
                                pauseCommand = 15;
                            }
                            break;
                        }
                        case 16: { //step out with counter
                            stream.seek(this);
                            executor.execute(this, stream);
                            if (executor.ins == Call) {
                                intObj[0]++;
                            } else if (executor.ins == Ret) {
                                intObj[0]--;
                            }
                            if (intObj[0] > 0) {
                                pauseCommand = 16;
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
