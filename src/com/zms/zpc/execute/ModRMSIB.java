package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.assembler.Assembler;
import com.zms.zpc.support.NotImplException;

/**
 * Created by 张小美 on 17/六月/29.
 * Copyright 2002-2016
 */
public class ModRMSIB {

    public boolean reg8;

    public String address;
    public int addressType;
    public String reg;
    public String addressReg;
    public long disp;
    public int opWidth;
    public int addressWidth;

    public String getR8(int reg) {
        return (String) Assembler.ModData[3][0][reg];
    }

    public String parseReg(Instruction instruction, int bits, int reg) {
        String result = null;
        if (this.reg8) {
            this.opWidth = 8;
            if (bits == 64 && instruction.isHasRex40()) {
                result = (String) Assembler.ModData[3][9][reg];
            } else {
                result = (String) Assembler.ModData[3][0][reg];
            }
        } else {
            if (this.opWidth < 0) {
                this.opWidth = instruction.getOpWidth(bits);
            }
            int width = this.opWidth;
            switch (width) {
                case 8:
                    result = getR8(reg);
                    break;
                case 16:
                    result = (String) Assembler.ModData[3][1][reg];
                    break;
                case 32:
                    result = (String) Assembler.ModData[3][2][reg];
                    break;
                case 64:
                    result = (String) Assembler.ModData[3][3][reg];
                    break;
            }
        }
        return result;
    }

    public void parse(Instruction instruction, CodeStream input, int bits) {
        address = null;
        addressType = -100;
        reg = null;
        addressReg = null;
        disp = 0;
        opWidth = -1;

        int ModRM = input.read();
        int mod = ModRM >> 6;
        int reg = (ModRM >> 3) & 0b111;
        int rm = ModRM & 0b111;

        this.reg = parseReg(instruction, bits, reg);
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
                    this.disp = instruction.read16(input);
                } else if (mod == 1) {
                    this.disp = instruction.read8(input);
                } else if (mod == 2) {
                    this.disp = instruction.read16(input);
                }
            } else if (width == 32) {
                this.address = (String) Assembler.ModData[2][1][rm];
                int base = 100;
                if (mod == 0 && rm == 0b101) {
                    this.address = "0";
                    this.disp = instruction.read32(input);
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
                    this.disp = instruction.read8(input);
                } else if (mod == 2) {
                    this.disp = instruction.read32(input);
                } else if (mod == 0 && rm == 0b100 && base == 0b101) {
                    this.disp = instruction.read32(input);
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
        throw new NotImplException();
    }

    public int setValReg(PC pc, long val) {
        return setValReg(pc, this.reg, val);
    }

    public int setValReg(PC pc, String reg, long val) {
        return pc.cpu.regs.getReg(reg).setValue(this.opWidth,val);
    }

    public int setValMemory(PC pc, long val) {
        if (addressReg != null) {
            return setValReg(pc, this.addressReg, val);
        }
        throw new NotImplException();
    }

}
