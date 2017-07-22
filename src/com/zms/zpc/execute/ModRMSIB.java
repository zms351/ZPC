package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.assembler.Assembler;
import com.zms.zpc.emulator.reg.Segment;
import com.zms.zpc.support.*;

/**
 * Created by 张小美 on 17/六月/29.
 * Copyright 2002-2016
 */
public class ModRMSIB {

    Instruction instru;

    public ModRMSIB(Instruction instru) {
        this.instru = instru;
    }

    public boolean reg8;

    public String address;
    public int addressType;
    public String reg;
    public int regIndex;
    public String addressReg;
    public long disp;
    public int dispWidth;
    public int opWidth;
    public int regType;

    private int addressWidth;

    public String parseReg(Instruction instruction, int bits, int reg) {
        if (this.reg8) {
            this.opWidth = 8;
            if (bits == 64 && instruction.isHasRex40()) {
                return (String) Assembler.ModData[3][9][reg];
            } else {
                return (String) Assembler.ModData[3][0][reg];
            }
        } else {
            if (this.opWidth < 0) {
                this.opWidth = instruction.getOpWidth(bits);
            }
            instruction.__width = this.opWidth;
            int width = this.opWidth;
            switch (width) {
                case 8:
                    return (String) Assembler.ModData[3][0][reg];
                case 16:
                    return (String) Assembler.ModData[3][1][reg];
                case 32:
                    return (String) Assembler.ModData[3][2][reg];
                case 64:
                    return (String) Assembler.ModData[3][3][reg];
            }
        }
        throw new NotImplException();
    }

    public void parse(Instruction instruction, CodeStream input, int bits) {
        address = null;
        addressType = -100;
        reg = null;
        addressReg = null;
        disp = 0;

        int ModRM = input.read();
        int mod = ModRM >> 6;
        int reg = regIndex = (ModRM >> 3) & 0b111;
        int rm = ModRM & 0b111;

        switch (regType) {
            case 0:
                this.reg = parseReg(instruction, bits, reg);
                break;
            case 1:
                this.reg = (String) Assembler.ModData[3][6][reg];
                break;
            default:
                throw new NotImplException();
        }
        assert this.reg != null;

        if (mod == 0b11) {
            this.addressReg = parseReg(instruction, bits, rm);
            this.addressType = -1;
        } else {
            int width = addressWidth = instruction.getAddressWidth(bits);
            this.addressType = 1600 + mod * 10 + rm;
            if (width == 16) {
                this.address = (String) Assembler.ModData[2][0][rm];
                if (mod == 0 && rm == 0b110) {
                    this.address = "0";
                    this.disp = instruction.read16();
                    dispWidth = 16;
                } else if (mod == 1) {
                    this.disp = instruction.read8();
                    dispWidth = 8;
                } else if (mod == 2) {
                    this.disp = instruction.read16();
                    dispWidth = 16;
                }
            } else if (width == 32) {
                this.address = (String) Assembler.ModData[2][1][rm];
                int base = 100;
                if (mod == 0 && rm == 0b101) {
                    this.address = "0";
                    this.disp = instruction.read32();
                    dispWidth = 32;
                } else if (rm == 0b100) {
                    int sib = input.read();
                    int scale = sib >> 6;
                    base = sib & 0b111;
                    int index = (sib >> 3) & 0b111;
                    String ca;
                    switch (scale) {
                        case 1:
                            ca = "*2";
                            break;
                        case 2:
                            ca = "*4";
                            break;
                        case 3:
                            ca = "*8";
                            break;
                        default:
                            ca = "";
                            break;
                    }
                    if (index == 0b100 && mod == 0 && base == 0b101) {
                        this.address = "0";
                    } else if (index == 0b100) {
                        //no index
                        this.address = (String) Assembler.ModData[2][1][base];
                    } else if (mod == 0 && base == 0b101) {
                        //no base
                        this.address = Assembler.ModData[2][1][index] + ca;
                    } else {
                        this.address = Assembler.ModData[2][1][index] + ca + "+" + Assembler.ModData[2][1][base];
                    }
                }
                if (mod == 1) {
                    this.disp = instruction.read8();
                    dispWidth = 8;
                } else if (mod == 2) {
                    this.disp = instruction.read32();
                    dispWidth = 32;
                } else if (mod == 0 && rm == 0b100 && base == 0b101) {
                    this.disp = instruction.read32();
                    dispWidth = 32;
                }
            } else {
                throw new NotImplException();
            }
        }
        assert addressType > -50;
    }

    public long getValReg(PC pc) {
        return getValReg(pc, this.reg);
    }

    public long getValReg(PC pc, String reg) {
        return pc.cpu.regs.getReg(reg).getValue(this.opWidth);
    }

    public long getValMemory(PC pc) {
        if (addressReg != null) {
            return getValReg(pc, this.addressReg);
        }
        long address = getMemoryAddress(pc);
        return memoryRead(pc, address, this.opWidth);
    }

    public int setValReg(PC pc, long val) {
        return setValReg(pc, this.reg, val);
    }

    public int setValReg(PC pc, String reg, long val) {
        return pc.cpu.regs.getReg(reg).setValue(this.opWidth, val);
    }

    public int setValMemory(PC pc, long val) {
        if (addressReg != null) {
            return setValReg(pc, this.addressReg, val);
        }
        long address = getMemoryAddress(pc);
        memoryWrite(pc, address, val, this.opWidth);
        return this.opWidth;
    }

    public void memoryWrite(PC pc, long address, long val, int width) {
        pc.memory.write(0, address, val, width);
    }

    public long memoryRead(PC pc, long address, int width) {
        return pc.memory.read(0, address, width);
    }

    private boolean addressUseReg;

    public long getMemoryAddress(PC pc) {
        assert addressReg == null;
        addressUseReg = false;
        long address = calAddress(pc, this.address);
        if (addressUseReg) {
            //disp signed
            address += NumberUtils.asSigned(this.disp, this.dispWidth);
        } else {
            address += this.disp;
        }
        Segment seg = (Segment) pc.cpu.regs.getReg(instru.segBase);
        return seg.getAddress(address);
    }

    private long calAddress(PC pc, String expr) {
        if (expr == null || (expr = expr.trim()).length() < 1) {
            return 0;
        }
        if (expr.length() == 1) {
            return Integer.parseInt(expr);
        }
        int index = expr.indexOf('+');
        if (index > 0) {
            return calAddress(pc, expr.substring(0, index).trim()) + calAddress(pc, expr.substring(index + 1).trim());
        }
        index = expr.indexOf('*');
        if (index > 0) {
            return calAddress(pc, expr.substring(0, index).trim()) * calAddress(pc, expr.substring(index + 1).trim());
        }
        addressUseReg = true;
        return pc.cpu.regs.getReg(expr).getValue(addressWidth);
    }

    public int getAddressWidth() {
        return addressWidth;
    }

}
