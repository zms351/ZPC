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
        pc.cpu.regs.ip.getRegWithWidth(getOpWidth()).setValue(offset);
        pc.cpu.regs.cs.setValue(base);
    }

    public void executeJumpShort() {
        long offset = readOp();
        offset = NumberUtils.asSigned(offset, getOpWidth());
        BaseReg rip = pc.cpu.regs.rip;
        rip.setValue(rip.getValue() + offset);
    }

    public boolean executeJumpNear() {
        long offset = mrs.getValMemory(pc);
        BaseReg rip = pc.cpu.regs.rip;
        rip.setValue(offset);
        return true;
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
            case 0x6:
                return bits.cf() || bits.zf();
            case 0x7:
                return (!bits.cf()) && (!bits.zf());
            case 0x8:
                return bits.sf();
            case 0xc:
                return bits.sf()!=bits.of();
            case 0xd:
                return bits.sf()==bits.of();
            case 0xe:
                return bits.zf() || (bits.sf() != bits.of());
            case 0xf:
                return !bits.zf() && bits.sf() == bits.of();
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
        long jump = NumberUtils.asSigned(input.read(), 8);
        if (!out) {
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

    public void exeucutePush68() {
        read0();
        push_(__v1, __width);
    }

    public void exeucutePush6a() {
        long v = NumberUtils.asSigned(input.read(), 8);
        push_(__v1, getOpWidth());
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

    public void executeMov(int op, int mr, int rm) {
        if (op < 2) {
            op = getOpcode(op);
        }
        if (op == mr) {
            executeMovMR();
        } else if (op == rm) {
            executeMovRM();
        } else {
            throw new NotImplException();
        }
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
                executor.checkIR(false);
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

        if (isHasf3()) {
            BaseReg cr = regs.cx.getRegWithWidth(getAddressWidth(executor.getBits()));
            long c = cr.getValue();
            while (c != 0) {
                executor.checkIR(false);
                long val = mrs.memoryRead(pc, base.getAddress(off), width);
                reg.setValue(width, val);
                off = df ? off - n : off + n;
                c--;
            }
            cr.setValue(0);
            offr.setValue(off);
        } else {
            long val = mrs.memoryRead(pc, base.getAddress(off), width);
            reg.setValue(width, val);
            off = df ? off - n : off + n;
            offr.setValue(off);
        }
    }

    public boolean executeCallFar() {
        assert pc.cpu.getMode()==CPUMode.Real;
        int width=getOpWidth();
        long address = mrs.getMemoryAddress(pc);
        executor.reLoc(input);
        Regs regs = pc.cpu.regs;
        push_(regs.cs.getValue(),width);
        push_(regs.rip.getValue(),width);
        regs.rip.getRegWithWidth(width).setValue(mrs.memoryRead(pc,address,width));
        int n=width/8;
        assert n*8==width;
        regs.cs.setValue(16,mrs.memoryRead(pc,address+n,16));
        return true;
    }

    public void executeCallShort() {
        int width = getOpWidth();
        long offset = NumberUtils.asSigned(readOp(width), width);
        call_(offset, false);
    }

    protected void call_(long offset, boolean replace) {
        BaseReg rip = pc.cpu.regs.rip;
        executor.reLoc(input);
        long from = rip.getValue();
        push_(rip.getValue(), getOpWidth());
        long to = replace ? offset : (rip.getValue() + offset);
        pc.getDebugger().onMessage(DEBUG, "Call Near from %H to %H\n", from, to);
        rip.setValue(to);
        executor.ins = Call;
    }

    public boolean executeCallNear() {
        long offset = mrs.getValMemory(pc);
        call_(offset, true);
        return true;
    }

    public void executeRetNear() {
        int width = getOpWidth(executor.getBits());
        long v = pop_(width);
        pc.getDebugger().onMessage(DEBUG, "Ret to %H\n", v);
        pc.cpu.regs.rip.setValue(v);
        executor.ins = Ret;
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

    public void executeMov6(BaseReg reg, boolean rm) {
        int width = getOpWidth();
        reg = reg.getRegWithWidth(width);
        int addressWidth = getAddressWidth(executor.getBits());
        long address = readOp(addressWidth);
        address = mrs.getMemoryAddress(pc, address);
        if (rm) {
            long v = mrs.memoryRead(pc, address, width);
            reg.setValue(width, v);
        } else {
            long v = reg.getValue(width);
            mrs.memoryWrite(pc, address, v, width);
        }
    }

    public void executeMov7() {
        read0();
        mrs.setValMemory(pc, __v1);
    }

    public void executeMov8() {
        long v = mrs.getValMemory(pc, 8) & 0xff;
        mrs.setValReg(pc, v);
    }

    public void executeMov9() {
        long v = mrs.getValMemory(pc, 16) & 0xffff;
        mrs.setValReg(pc, v);
    }

    public void executeMova() {
        long v = NumberUtils.asSigned(mrs.getValMemory(pc, 8), 8);
        mrs.setValReg(pc, v);
    }

    public void executeMovb() {
        long v = NumberUtils.asSigned(mrs.getValMemory(pc, 16), 16);
        mrs.setValReg(pc, v);
    }

    public void executeMovc() {
        long v = NumberUtils.asSigned(mrs.getValMemory(pc, 32), 32);
        mrs.setValReg(pc, v);
    }

    public void executeLSegment(Segment seg) {
        int width = getOpWidth();
        int n = width / 8;
        assert n * 8 == width;
        mrs.disp += n;
        seg.setValue(mrs.getValMemory(pc));
        mrs.disp -= n;
        mrs.setValReg(pc, mrs.getValMemory(pc));
    }

    public boolean executeInt(long v) {
        if (v == -1) {
            v = readOp(8);
        }
        if (v == 4) {
            if (!bits.of()) {
                return false;
            }
        }

        Regs regs = pc.cpu.regs;
        push_(regs.eflags.getValue(), 16);
        bits.clearITACR();

        BaseReg rip = pc.cpu.regs.rip;
        executor.reLoc(input);
        push_(regs.cs.getValue(), 16);
        push_(regs.rip.getValue(), 16);

        regs.rip.setValue(mrs.memoryRead(pc, 4 * v, 16));
        regs.cs.setValue(mrs.memoryRead(pc, 4 * v + 2, 16));

        return true;
    }

    public boolean executeIRet() {
        Regs regs = pc.cpu.regs;
        regs.rip.setValue(pop_(16));
        regs.cs.setValue(pop_(16));
        regs.flags.setValue(pop_(16));
        return true;
    }

    public void executeHlt() {
        executor.checkIR(true);
    }

    private void loadBigSegment(BigSegment reg) {
        int width = getOpWidth();
        long address = mrs.getMemoryAddress(pc);
        reg.limit = mrs.memoryRead(pc, address, 16);
        switch (width) {
            case 16:
                reg.setValue(mrs.memoryRead(pc, address + 2, 32) & 0xffffff);
                break;
            case 32:
                reg.setValue(mrs.memoryRead(pc, address + 2, 32));
                break;
            case 64:
                reg.setValue(mrs.memoryRead(pc, address + 2, 64));
                break;
            default:
                throw new NotImplException();
        }
    }

    public void executeMem1() {
        Regs regs = pc.cpu.regs;
        switch (getOpcode(1)) {
            case 1:
                switch (mrs.regIndex) {
                    case 2:
                        loadBigSegment(regs.gdtr);
                        break;
                    case 3:
                        loadBigSegment(regs.idtr);
                        break;
                    default:
                        throw new NotImplException();
                }
                break;
            default:
                throw new NotImplException();
        }
    }

    public void executeMovs() {
        int addressWidth = getAddressWidth(executor.getBits());
        Regs regs = pc.cpu.regs;
        boolean df = bits.df.get();

        Segment seg1 = (Segment) regs.getReg(getSegBase());
        BaseReg off1 = regs.si.getRegWithWidth(addressWidth);
        long address1 = seg1.getAddress(off1);

        Segment seg2 = regs.es;
        BaseReg off2 = regs.di.getRegWithWidth(addressWidth);
        long address2 = seg2.getAddress(off2);

        int opWidth = getOpWidth();
        int n = opWidth / 8;
        assert n * 8 == opWidth;

        if (isHasf3()) {
            BaseReg cr = regs.cx.getRegWithWidth(getAddressWidth(executor.getBits()));
            long c = cr.getValue();
            long c1 = c;
            while (c != 0) {
                executor.checkIR(false);
                long v = mrs.memoryRead(pc, address1, opWidth);
                mrs.memoryWrite(pc, address2, v, opWidth);
                address1 = df ? address1 - n : address1 + n;
                address2 = df ? address2 - n : address2 + n;
                c--;
            }
            cr.setValue(0);
            off1.setValue(df ? (off1.getValue() - n * c1) : (off1.getValue() + n * c1));
            off2.setValue(df ? (off2.getValue() - n * c1) : (off2.getValue() + n * c1));
        } else {
            long v = mrs.memoryRead(pc, address1, opWidth);
            mrs.memoryWrite(pc, address2, v, opWidth);
            off1.setValue(df ? (off1.getValue() - n) : (off1.getValue() + n));
            off2.setValue(df ? (off2.getValue() - n) : (off2.getValue() + n));
        }
    }

    public void executeCPUID() {
        Regs regs = pc.cpu.regs;
        switch ((int) regs.eax.getValue()) {
            case 0x00:
                regs.eax.setValue(0x02);
                regs.ebx.setValue(0x756e6547); /* "Genu", with G in the low nibble of BL */
                regs.edx.setValue(0x49656e69); /* "ineI", with i in the low nibble of DL */
                regs.ecx.setValue(0x6c65746e); /* "ntel", with n in the low nibble of CL */
                return;
            case 0x01:
                regs.eax.setValue(0x634);
                regs.ebx.setValue(1 << 16);
                regs.ecx.setValue(0);

                int features = 0;
                features |= 1; //Have an FPU;
                features |= (1 << 1);  // VME - Virtual 8086 mode enhancements, CR4.VME and eflags.VIP and VIF
                features |= (1 << 2); // Debugging extensions CR4.DE and DR4 and DR5
                features |= (1 << 3);  // Support Page-Size Extension (4M pages)

                features |= (1 << 4);  // implement TSC
                //features |= (1<< 5);  // support RDMSR/WRMSR
                features |= (1 << 6);  // Support PAE.
                features |= (1 << 7);  // Machine Check exception

                features |= (1 << 8);  // Support CMPXCHG8B instruction - Bochs doesn't have this!
                //features |= (1<< 9);   // APIC on chip
                // (1<<10) is reserved
                features |= (1 << 11);  // SYSENTER/SYSEXIT

                //features |= (1<<12);  // Memory type range registers (MSR)
                features |= (1 << 13);  // Support Global pages.
                features |= (1 << 14);  // Machine check architecture
                features |= (1 << 15);  // Implement CMOV instructions.

                features |= (1 << 23);  // support MMX
                features |= (1 << 28);  // max APIC ID (cpuid.1.ebx[23-16]) is valid
                regs.edx.setValue(features);
                return;
            case 0x02:
                regs.eax.setValue(0x3020101);
                regs.ebx.setValue(0);
                regs.ecx.setValue(0);
                regs.edx.setValue(0xc040843);
                return;
            default:
                throw new NotImplException();
        }
    }

    public void executePusha() {
        int width = getOpWidth();
        Regs regs = pc.cpu.regs;
        long v = regs.esp.getValue();
        switch (width) {
            case 16:
            case 32:
                push_(regs.eax.getValue(), width);
                push_(regs.ecx.getValue(), width);
                push_(regs.edx.getValue(), width);
                push_(regs.ebx.getValue(), width);
                push_(v, width);
                push_(regs.ebp.getValue(), width);
                push_(regs.esi.getValue(), width);
                push_(regs.edi.getValue(), width);
                break;
            default:
                throw new NotImplException();
        }
    }

    public void executePopa() {
        int width = getOpWidth();
        Regs regs = pc.cpu.regs;
        switch (width) {
            case 16:
            case 32:
                regs.rdi.getRegWithWidth(width).setValue(pop_(width));
                regs.rsi.getRegWithWidth(width).setValue(pop_(width));
                regs.rbp.getRegWithWidth(width).setValue(pop_(width));
                pop_(width);
                regs.rbx.getRegWithWidth(width).setValue(pop_(width));
                regs.rdx.getRegWithWidth(width).setValue(pop_(width));
                regs.rcx.getRegWithWidth(width).setValue(pop_(width));
                regs.rax.getRegWithWidth(width).setValue(pop_(width));
                break;
            default:
                throw new NotImplException();
        }
    }

    public void executePushf() {
        long val=pc.cpu.regs.getFlag();
        push_(val,getOpWidth());
    }

}
