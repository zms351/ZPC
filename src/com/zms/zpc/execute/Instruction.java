package com.zms.zpc.execute;

import com.zms.zpc.support.NotImplException;

/**
 * Created by 张小美 on 17/六月/27.
 * Copyright 2002-2016
 */
public class Instruction {

    private long startPos;
    private int legacyPrefixCount;
    private int[] legacyPrefix = new int[20];

    private int rexPrefixCount;
    private int[] rexPrefix = new int[20];

    private boolean has66, has67;

    private int opcodeCount;
    private int[] opcode = new int[20];
    private boolean[] reg8Ops = new boolean[256];

    public Instruction() {
        reg8Ops[0x8a] = true; //mov
        reg8Ops[0x30] = true; //xor
    }

    public void parse1(CodeStream input, int bits) {
        legacyPrefixCount = 0;
        rexPrefixCount = 0;
        int n;
        has66 = has67 = false;
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
                    break;
                case 0x3e:
                    legacyPrefix[legacyPrefixCount++] = n;
                    break;
                case 0x26:
                    legacyPrefix[legacyPrefixCount++] = n;
                    break;
                case 0x64:
                    legacyPrefix[legacyPrefixCount++] = n;
                    break;
                case 0x65:
                    legacyPrefix[legacyPrefixCount++] = n;
                    break;
                case 0x36:
                    legacyPrefix[legacyPrefixCount++] = n;
                    break;
                case 0xf3:
                    legacyPrefix[legacyPrefixCount++] = n;
                    break;
                case 0xf2:
                    legacyPrefix[legacyPrefixCount++] = n;
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

    public ModRMSIB mrs = new ModRMSIB();

    public void parse2(CodeStream input, int bits) {
        mrs.reg8 = reg8Ops[opcode[0]];
        mrs.parse(this, input, bits);
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

    public int[] getOpcode() {
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

}
