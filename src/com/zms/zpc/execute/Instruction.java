package com.zms.zpc.execute;

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

    public Instruction() {
    }

    public void parse1(CodeInputStream input, int bits) {
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

    public void parse2(CodeInputStream input, int bits) {
        int ModRM = input.read();
        int mod = ModRM >> 6;
        int reg = (ModRM >> 3) & 0b111;
        int rm = ModRM & 0b111;
        System.out.println("here");
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

}
