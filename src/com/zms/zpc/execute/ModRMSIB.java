package com.zms.zpc.execute;

import com.zms.zpc.emulator.assembler.Assembler;

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

    public String parseReg(Instruction instruction, int bits, int reg) {
        String result = null;
        if (this.reg8) {
            if (bits == 64 && instruction.isHasRex40()) {
                result = (String) Assembler.ModData[3][9][reg];
            } else {
                result = (String) Assembler.ModData[3][0][reg];
            }
        } else {
            int width = instruction.getOpWidth(bits);
            switch (width) {
                case 8:
                    result = (String) Assembler.ModData[3][0][reg];
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

    public void parse(Instruction instruction, CodeInputStream input, int bits) {
        address = null;
        addressType = -1;
        reg = null;
        addressReg = null;

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

        }
    }

}
