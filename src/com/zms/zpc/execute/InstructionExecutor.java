package com.zms.zpc.execute;

import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.processor.*;
import com.zms.zpc.emulator.reg.*;
import com.zms.zpc.support.*;

/**
 * Created by 张小美 on 17/六月/27.
 * Copyright 2002-2016
 */
public abstract class InstructionExecutor extends Instruction {

    public Bits bits;
    public PC pc;

    public long readOp() {
        int width = getOpWidth();
        return readOp(width);
    }

    public long readOp(int width) {
        switch (width) {
            case 8:
                return input.read();
            case 16:
                return read16();
            case 32:
                return read32();
            case 64:
                return read64();
        }
        throw new NotImplException();
    }

    public void executeJumpFar() {
        long offset = readOp();
        int base = read16();
        if (executor.getBits() == 16) {
            pc.cpu.regs.cs.setValue(base);
            pc.cpu.regs.rip.setValue64(offset);
        } else {
            throw new NotImplException();
        }
    }

    public void executeJumpNear() {
        long offset = readOp();
        offset = NumberUtils.asSigned(offset, getOpWidth());
        BaseReg rip = pc.cpu.regs.rip;
        rip.setValue(rip.getValue() + offset);
    }

    public boolean testCondition(int code) {
        code = code & 0xf;
        switch (code) {
            case 0x2:
                return bits.cf();
            case 0x3:
                return !bits.cf();
            case 0x4:
                return bits.zf();
            case 0x5:
                return !bits.zf();
            case 0x7:
                return (!bits.cf()) && (!bits.zf());
            default:
                throw new NotImplException();
        }
    }

    public boolean executeJcc() {
        int jump = (byte) input.read();
        if (!testCondition(getOpcode())) {
            jump = 0;
        }
        if (jump != 0) {
            BaseReg rip = pc.cpu.regs.rip;
            rip.setValue(rip.getValue() + jump);
        }
        return false;
    }

    public boolean executeLoop(int type) {
        boolean out;
        switch (type) {
            case 1:
                BaseReg reg = getReg(pc, mrs.parseReg(this, executor.getBits(), 1));
                long v = reg.getValue() - 1;
                reg.setValue(v);
                out = v == 0;
                break;
            default:
                throw new NotImplException();
        }
        BaseReg rip = pc.cpu.regs.rip;
        long jump=NumberUtils.asSigned(input.read(), 8);
        if(!out) {
            rip.setValue(rip.getValue() + jump);
        }
        return false;
    }

    public boolean executeJcc2() {
        long jump = readOp();
        jump = NumberUtils.asSigned(jump, getOpWidth());
        if (!testCondition(getOpcode(1))) {
            jump = 0;
        }
        if (jump != 0) {
            BaseReg rip = pc.cpu.regs.rip;
            rip.setValue(rip.getValue() + jump);
        }
        return false;
    }

    protected long __v1, __v2;
    protected BaseReg __reg;
    protected int __width, __v3;

    protected void read0() {
        int width = getOpWidth(executor.getBits());
        if (width == 64) {
            __v1 = NumberUtils.signExtend32_2_64(readOp(32));
        } else {
            __v1 = readOp(width);
        }
        __width = width;
    }

    protected void read1() {
        read0();
        __reg = getReg(pc, mrs.parseReg(this, executor.getBits(), 0));
    }

    protected void read2(int a, int b, int c, int d) {
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
            __v3 = pc.cpu.regs.dx.getValue16();
        }
        __reg = getReg(pc, mrs.parseReg(this, executor.getBits(), 0));
    }

    public void executeOut() {
        read2(0xe6, 0xee, 0xe6, 0xe7);
        pc.board.ios.write(__v3, __reg.getValue(), __width);
    }

    public void executeIn() {
        read2(0xe4, 0xec, 0xe4, 0xe5);
        __reg.setValue(__width, pc.board.ios.read(__v3, __width));
    }

    public void executeMovri(int base) {
        int r = getOpcode() - base;
        BaseReg reg = getReg(pc, mrs.parseReg(this, executor.getBits(), r));
        long v = readOp();
        reg.setValue(v);
    }

    public void executePush50() {
        int r = getOpcode() - 0x50;
        BaseReg reg = getReg(pc, mrs.parseReg(this, executor.getBits(), r));
        executePush(reg, mrs.opWidth);
    }

    public void executePushff() {
        long val = mrs.getValMemory(pc);
        push_(val, mrs.opWidth);
    }

    public void executePop58() {
        int r = getOpcode() - 0x58;
        BaseReg reg = getReg(pc, mrs.parseReg(this, executor.getBits(), r));
        executePop(reg, mrs.opWidth);
    }

    public void executePop(BaseReg reg, int width) {
        long val = pop_(width);
        reg.setValue(val);
    }

    public void executePush(BaseReg reg, int width) {
        long val = reg.getValue();
        push_(val, width);
    }

    public void executePop8f() {
        long val = pop_(mrs.opWidth);
        mrs.setValMemory(pc, val);
    }

    public void push_(long val, int width) {
        Regs regs = pc.cpu.regs;
        BaseReg rsp = regs.rsp;
        int n = width / 8;
        assert n * 8 == width;
        rsp.setValue(rsp.getValue() - n);
        long address = regs.ss.getAddress(rsp);
        pc.getDebugger().onMessage(DEBUG, "push on %H %H %H\n", regs.ss.getValue(), rsp.getValue(), val);
        mrs.memoryWrite(pc, address, val, width);
    }

    public long pop_(int width) {
        Regs regs = pc.cpu.regs;
        BaseReg rsp = regs.rsp;
        int n = width / 8;
        assert n * 8 == width;
        long address = regs.ss.getAddress(rsp);
        long v = mrs.memoryRead(pc, address, width);
        pc.getDebugger().onMessage(DEBUG, "pop on %H %H %H\n", regs.ss.getValue(), rsp.getValue(), v);
        rsp.setValue(rsp.getValue() + n);
        return v;
    }

    public void executeMovMR() {
        long v = mrs.getValReg(pc);
        mrs.setValMemory(pc, v);
    }

    public void executeMovRM() {
        long v = mrs.getValMemory(pc);
        mrs.setValReg(pc, v);
    }

    public void executeCMC() {
        bits.cf.not();
        bits.setStatus(bits.getStatus() & NCF);
    }

    public void executeCF_(boolean set) {
        bits.cf.set(set);
        bits.setStatus(bits.getStatus() & NCF);
    }

    public void executeIF_(boolean set) {
        bits.if_.set(set);
    }

    public void executeDF_(boolean set) {
        bits.df.set(set);
    }

    public void executeSTOS() {
        Regs regs = pc.cpu.regs;
        BaseReg reg = getReg(pc, mrs.parseReg(this, executor.getBits(), 0));
        Segment base = regs.es;
        BaseReg offr = regs.di.getRegWithWidth(getAddressWidth(executor.getBits()));

        long val = reg.getValue();
        int width = mrs.opWidth;
        long off = offr.getValue();
        boolean df = bits.df.get();
        int n = width / 8;
        assert n * 8 == width;

        if (isHasf2() || isHasf3()) {
            BaseReg cr = regs.cx.getRegWithWidth(getAddressWidth(executor.getBits()));
            long c = cr.getValue();
            while (c != 0) {
                executor.checkIR();
                executeSTOS_(base, off, val, width);
                off = df ? off - n : off + n;
                c--;
            }
            cr.setValue(0);
            offr.setValue(off);
        } else {
            executeSTOS_(base, off, val, width);
            off = df ? off - n : off + n;
            offr.setValue(off);
        }
    }

    public void executeSTOS_(Segment base, long off, long val, int width) {
        long address = base.getAddress(off);
        mrs.memoryWrite(pc, address, val, width);
    }

    public void executeLODS() {
        Regs regs = pc.cpu.regs;
        BaseReg reg = getReg(pc, mrs.parseReg(this, executor.getBits(), 0));
        Segment base = regs.ds;
        BaseReg offr = regs.si.getRegWithWidth(getAddressWidth(executor.getBits()));

        int width = mrs.opWidth;
        long off = offr.getValue();
        boolean df = bits.df.get();
        int n = width / 8;
        assert n * 8 == width;

        if (isHasf2() || isHasf3()) {
            BaseReg cr = regs.cx.getRegWithWidth(getAddressWidth(executor.getBits()));
            long c = cr.getValue();
            while (c != 0) {
                executor.checkIR();
                executeLODS_(base, off, width, reg);
                off = df ? off - n : off + n;
                c--;
            }
            cr.setValue(0);
            offr.setValue(off);
        } else {
            executeLODS_(base, off, width, reg);
            off = df ? off - n : off + n;
            offr.setValue(off);
        }
    }

    public void executeLODS_(Segment base, long off, int width, BaseReg reg) {
        long val = mrs.memoryRead(pc, base.getAddress(off), width);
        reg.setValue(width, val);
    }

    public void executeCallNear() {
        int width = getOpWidth(executor.getBits());
        long offset = NumberUtils.asSigned(readOp(width), width);
        BaseReg rip = pc.cpu.regs.rip;
        executor.reLoc(input);
        long from = rip.getValue();
        push_(rip.getValue(), width);
        long to = rip.getValue() + offset;
        pc.getDebugger().onMessage(DEBUG, "Call Near from %H to %H\n", from, to);
        rip.setValue(to);
    }

    public void executeRetNear() {
        int width = getOpWidth(executor.getBits());
        long v = pop_(width);
        pc.getDebugger().onMessage(DEBUG, "Ret to %H\n", v);
        pc.cpu.regs.rip.setValue(v);
    }

    public void executeLEA() {
        int opWidth = getOpWidth();
        int addressWidth = getAddressWidth(executor.getBits());
        long address = mrs.getMemoryAddress(pc);
        if (addressWidth < opWidth) {
            address = NumberUtils.zeroExtend(address, addressWidth);
        }
        mrs.setValReg(pc, address);
    }

    public void executeMov6(BaseReg reg,boolean rm) {
        int width=getOpWidth();
        reg=reg.getRegWithWidth(width);
        int addressWidth=getAddressWidth(executor.getBits());
        long address=readOp(addressWidth);
        if(rm) {
            long v = mrs.memoryRead(pc, address, width);
            reg.setValue(width,v);
        } else {
            long v=reg.getValue(width);
            mrs.memoryWrite(pc,address,v,width);
        }
    }

    public void executeMov7() {
        read0();
        mrs.setValMemory(pc,__v1);
    }

    public void executeLds() {
        mrs.getMemoryAddress(pc);
        pc.cpu.regs.ds.setValue(mrs.addressCalSegment.getValue());
        mrs.setValReg(pc,mrs.addressCal);
    }

    public boolean executeInt(long v) {
        if(v==-1) {
            v=readOp(8);
        }
        if(v==4) {
            if(!bits.of()) {
                return false;
            }
        }

        Regs regs = pc.cpu.regs;
        push_(regs.eflags.getValue(),16);
        bits.clearITACR();

        BaseReg rip = pc.cpu.regs.rip;
        executor.reLoc(input);
        push_(regs.cs.getValue(),16);
        push_(regs.rip.getValue(),16);

        regs.rip.setValue(mrs.memoryRead(pc,4*v,16));
        regs.cs.setValue(mrs.memoryRead(pc,4*v+2,16));

        return false;
    }

    public boolean executeIRet() {
        Regs regs = pc.cpu.regs;
        regs.rip.setValue(pop_(16));
        regs.cs.setValue(pop_(16));
        regs.flags.setValue(pop_(16));
        return false;
    }

}
