package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.processor.Bits;
import com.zms.zpc.emulator.reg.BaseReg;
import com.zms.zpc.support.*;

/**
 * Created by 张小美 on 17/六月/27.
 * Copyright 2002-2016
 */
public class InstructionExecutor extends Instruction implements Constants {

    public Bits bits;

    public long readOp(CodeExecutor executor, CodeStream input) {
        int width = getOpWidth(executor.getBits());
        return readOp(input, width);
    }

    public long readOp(CodeStream input, int width) {
        switch (width) {
            case 8:
                return input.read();
            case 16:
                return read16(input);
            case 32:
                return read32(input);
            case 64:
                return read64(input);
        }
        throw new NotImplException();
    }

    public long signExtend32_2_64(long n) {
        return (int) n;
    }

    public long signExtend8_2_64(long n) {
        return (byte) n;
    }

    public long signExtend8_2_32(long n) {
        int v = (byte) n;
        return v & 0xffffffffL;
    }

    public long signExtend8_2_16(long n) {
        int v = (byte) n;
        return v & 0xffffL;
    }

    public long signExtend8(long n, int width) {
        switch (width) {
            case 8:
                return n & 0xff;
            case 16:
                return signExtend8_2_16(n);
            case 32:
                return signExtend8_2_32(n);
            case 64:
                return signExtend8_2_64(n);
            default:
                throw new NotImplException();
        }
    }

    public long zeroExtend32_2_64(long n) {
        return n & 0xffffffffL;
    }

    public void executeJumpFar(CodeExecutor executor, CodeStream input, PC pc) {
        long offset = readOp(executor, input);
        int base = read16(input);
        if (executor.getBits() == 16) {
            pc.cpu.regs.cs.setValue16(base, true);
            pc.cpu.regs.rip.setValue64(offset);
        } else {
            throw new NotImplException();
        }
    }

    public boolean executeJcc(CodeExecutor executor, CodeStream input, PC pc) {
        int jump = (byte) input.read();
        switch (getOpcode()) {
            case 0x74:
                if (!bits.zf()) {
                    jump = 0;
                }
                break;
            default:
                throw new NotImplException();
        }
        if (jump != 0) {
            BaseReg rip = pc.cpu.regs.rip;
            rip.setValue(rip.getValue() + jump);
        }
        return false;
    }

    public void executeXor30313233(CodeExecutor executor, CodeStream input, PC pc, boolean rm) {
        long v1 = mrs.getValMemory(pc);
        long v2 = mrs.getValReg(pc);
        long v = xor_(v1, v2);
        if (rm) {
            mrs.setValReg(pc, v);
        } else {
            mrs.setValMemory(pc, v);
        }
    }

    private long xor_(long v1, long v2) {
        long v = v1 ^ v2;
        bits.clearOCA();
        bits.result = v;
        bits.status = SZP;
        return v;
    }

    private long __v1, __v2;
    private BaseReg __reg;
    private int __width, __v3;

    private void read0(CodeExecutor executor, CodeStream input, PC pc) {
        int width = getOpWidth(executor.getBits());
        if (width == 64) {
            __v1 = signExtend32_2_64(readOp(input, 32));
        } else {
            __v1 = readOp(input, width);
        }
        __width = width;
    }

    private void read1(CodeExecutor executor, CodeStream input, PC pc) {
        read0(executor, input, pc);
        __reg = getReg(pc, mrs.parseReg(this, executor.getBits(), 0));
    }

    private void read2(CodeExecutor executor, CodeStream input, PC pc, int a, int b, int c, int d) {
        int op = getOpcode();
        int width = getOpWidth(executor.getBits());
        if (op == a || op == b) {
            width = 8;
        }
        if (width != 8 && width != 16 && width != 32) {
            throw new NotImplException();
        }
        __width = width;
        if (op == c || op == d) {
            __v3 = input.read();
        } else {
            __v3 = getReg(pc, "DX").getValue16();
        }
        __reg = getReg(pc, mrs.parseReg(this, executor.getBits(), 0));
    }

    public void executeXor3435(CodeExecutor executor, CodeStream input, PC pc) {
        read1(executor, input, pc);
        __reg.setValue(xor_(__v1, __reg.getValue()));
    }

    public void executeCmp3c3d(CodeExecutor executor, CodeStream input, PC pc) {
        read1(executor, input, pc);
        cmp_(__reg.getValue(), __v1);
    }

    public void executeCmp83(CodeExecutor executor, CodeStream input, PC pc) {
        long v2 = readOp(input, 8);
        v2 = signExtend8(v2, mrs.opWidth);
        long v1 = mrs.getValMemory(pc);
        cmp_(v1, v2);
    }

    public void executeCmp82(CodeExecutor executor, CodeStream input, PC pc) {
        read0(executor, input, pc);
        long v = mrs.getValMemory(pc);
        cmp_(v, __v1);
    }

    public void executeCmp_rm_mr(CodeExecutor executor, CodeStream input, PC pc, boolean rm) {
        long v1 = mrs.getValMemory(pc);
        long v2 = mrs.getValReg(pc);
        if (rm) {
            cmp_(v2, v1);
        } else {
            cmp_(v1, v2);
        }
    }

    private void cmp_(long v1, long v2) {
        bits.op1 = v1;
        bits.op2 = v2;
        bits.result = v1 - v2;
        bits.ins = SUB;
        bits.status = OSZAPC;
    }

    public void executeOut(CodeExecutor executor, CodeStream input, PC pc) {
        read2(executor, input, pc, 0xe6, 0xee, 0xe6, 0xe7);
        pc.board.ios.write(__v3, __reg.getValue(), __width);
    }

    public void executeIn(CodeExecutor executor, CodeStream input, PC pc) {
        read2(executor, input, pc, 0xe4, 0xec, 0xe4, 0xe5);
        __reg.setValue(__width, pc.board.ios.read(__v3, __width));
    }

    public void executeMovri(CodeExecutor executor, CodeStream input, PC pc, int base) {
        int r = getOpcode() - base;
        BaseReg reg = getReg(pc, mrs.parseReg(this, executor.getBits(), r));
        long v = readOp(executor, input);
        reg.setValue(v);
    }

    public void executeMov8rm(CodeExecutor executor, CodeStream input, PC pc) {
        long v = mrs.getValReg(pc);
        mrs.setValMemory(pc, v);
    }

}
