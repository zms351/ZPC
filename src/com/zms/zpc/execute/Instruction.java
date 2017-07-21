package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.reg.BaseReg;
import com.zms.zpc.support.*;

/**
 * Created by 张小美 on 17/六月/27.
 * Copyright 2002-2016
 */
public class Instruction implements Constants {

    private long startPos;
    private int legacyPrefixCount;
    private int[] legacyPrefix = new int[20];

    private int rexPrefixCount;
    private int[] rexPrefix = new int[20];

    private boolean has66, has67;
    private boolean hasf3, hasf2;

    private int opcodeCount;
    private int[] opcode = new int[20];

    public int __width;
    public String segBase;

    public Instruction() {
    }

    public void readNextOp(CodeStream input) {
        opcode[opcodeCount++] = input.read();
    }

    public void parse1(CodeStream input, int bits) {
        legacyPrefixCount = 0;
        rexPrefixCount = 0;
        int n;
        has66 = has67 = false;
        hasf3 = hasf2 = false;
        out:
        while (true) {
            n = input.read();
            switch (n) {
                case 0x66:
                    legacyPrefix[legacyPrefixCount++] = n;
                    has66 = true;
                    break;
                case 0x67:
                    legacyPrefix[legacyPrefixCount++] = n;
                    has67 = true;
                    break;
                case 0x2e:
                    legacyPrefix[legacyPrefixCount++] = n;
                    segBase = "CS";
                    break;
                case 0x3e:
                    legacyPrefix[legacyPrefixCount++] = n;
                    segBase = "DS";
                    break;
                case 0x26:
                    legacyPrefix[legacyPrefixCount++] = n;
                    segBase = "ES";
                    break;
                case 0x64:
                    legacyPrefix[legacyPrefixCount++] = n;
                    segBase = "FS";
                    break;
                case 0x65:
                    legacyPrefix[legacyPrefixCount++] = n;
                    segBase = "GS";
                    break;
                case 0x36:
                    legacyPrefix[legacyPrefixCount++] = n;
                    segBase = "SS";
                    break;
                case 0xf3:
                    legacyPrefix[legacyPrefixCount++] = n;
                    hasf3=true;
                    break;
                case 0xf2:
                    legacyPrefix[legacyPrefixCount++] = n;
                    hasf2=true;
                    break;
                case 0xf0:
                    legacyPrefix[legacyPrefixCount++] = n;
                    break;
                default:
                    break out;
            }
        }
        if (bits == 64) {
            while (true) {
                if (n >= 0x40 && n <= 0xf) {
                    rexPrefix[rexPrefixCount++] = n;
                } else {
                    break;
                }
                n = input.read();
            }
        }
        opcodeCount = 0;
        opcode[opcodeCount++] = n;
    }

    public ModRMSIB mrs = new ModRMSIB(this);

    public void parse2(CodeStream input, int bits) {
        mrs.parse(this, input, bits);
        if(mrs.opWidth<0) {
            mrs.opWidth=getOpWidth(bits);
        }
    }

    public int getLegacyPrefixCount() {
        return legacyPrefixCount;
    }

    public int[] getLegacyPrefix() {
        return legacyPrefix;
    }

    public int getRexPrefixCount() {
        return rexPrefixCount;
    }

    public int[] getRexPrefix() {
        return rexPrefix;
    }

    public int getOpcodeCount() {
        return opcodeCount;
    }

    public int getOpcode() {
        return getOpcode(0);
    }

    public int getOpcode(int index) {
        assert index<opcodeCount;
        return getOpcodes()[index];
    }

    public int[] getOpcodes() {
        return opcode;
    }

    public long getStartPos() {
        return startPos;
    }

    public void setStartPos(long startPos) {
        this.startPos = startPos;
    }

    public boolean isHas66() {
        return has66;
    }

    public boolean isHas67() {
        return has67;
    }

    public boolean isHasf3() {
        return hasf3;
    }

    public boolean isHasf2() {
        return hasf2;
    }

    public boolean isHasRex(int b) {
        for (int i = 0; i < rexPrefixCount; i++) {
            if ((rexPrefix[i] & b) == b) {
                return true;
            }
        }
        return false;
    }

    public boolean isHasRexW() {
        return isHasRex(0b01001000);
    }

    public boolean isHasRexR() {
        return isHasRex(0b01000100);
    }

    public boolean isHasRexX() {
        return isHasRex(0b01000010);
    }

    public boolean isHasRexB() {
        return isHasRex(0b01000001);
    }

    public boolean isHasRex40() {
        for (int i = 0; i < rexPrefixCount; i++) {
            if (rexPrefix[i] == 0x40) {
                return true;
            }
        }
        return false;
    }

    public int getOpWidth(int bits) {
        bits = _getOpWidth(bits);
        if (bits != 8 && bits != 16 && bits != 32 && bits != 64) {
            throw new NotImplException();
        }
        return bits;
    }

    private int _getOpWidth(int bits) {
        if (mrs.reg8) {
            return 8;
        }
        if (bits == 16) {
            if (isHas66()) {
                return 32;
            }
        }
        if (bits == 32) {
            if (isHas66()) {
                return 16;
            }
        }
        if (bits == 64) {
            if (isHasRexW()) {
                return 64;
            } else if (isHas66()) {
                return 16;
            } else {
                return 32;
            }
        }
        return bits;
    }

    public int getAddressWidth(int bits) {
        bits = _getAddressWidth(bits);
        if (bits != 16 && bits != 32 && bits != 64) {
            throw new NotImplException();
        }
        return bits;
    }

    private int _getAddressWidth(int bits) {
        if (bits == 16) {
            if (isHas67()) {
                return 32;
            }
        }
        if (bits == 32) {
            if (isHas67()) {
                return 16;
            }
        }
        if (bits == 64) {
            if (isHas67()) {
                return 32;
            }
        }
        return bits;
    }

    public long read64(CodeStream input) {
        return read32(input) | read32(input) << 32;
    }

    public long read32(CodeStream input) {
        int a = read16(input);
        long b = read16(input);
        return b << 16 | a;
    }

    public int read16(CodeStream input) {
        return input.read() | (input.read() << 8);
    }

    public int read8(CodeStream input) {
        return input.read();
    }

    public BaseReg getReg(PC pc, String name) {
        return pc.cpu.regs.getReg(name);
    }

}
