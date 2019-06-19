package com.zms.zpc.execute;

import com.zms.zpc.emulator.processor.Regs;
import com.zms.zpc.emulator.reg.*;
import com.zms.zpc.support.*;

/**
 * Created by 张小美 on 17/七月/22.
 * Copyright 2002-2016
 */
public class InstructionExecutor2 extends InstructionExecutor {

    public long inc_(long v) {
        bits.cf.set(bits.cf());
        bits.setData(v, 1, v + 1, ADD, getOpWidth(), NCF);
        return bits.getResult();
    }

    public long dec_(long v) {
        bits.cf.set(bits.cf());
        bits.setData(v, 1, v - 1, SUB, getOpWidth(), NCF);
        return bits.getResult();
    }

    public long mul_(int code) {
        Regs regs = pc.cpu.regs;
        long v1, v2, result;
        v2 = mrs.getValMemory(pc);
        int width = getOpWidth();
        switch (width) {
            case 8:
                v1 = regs.al.getValue();
                break;
            case 16:
            case 32:
            case 64:
                v1 = regs.al.getRegWithWidth(width).getValue();
                break;
            default:
                throw new NotImplException();
        }
        switch (code) {
            case MUL:
                result = v1 * v2;
                break;
            case IMUL:
                result = NumberUtils.asSigned(v1, width) * NumberUtils.asSigned(v2, width);
                break;
            default:
                throw new NotImplException();
        }
        long half;
        switch (width) {
            case 8:
                regs.ax.setValue(result);
                half = result >> 8;
                break;
            case 16:
                regs.ax.setValue(result);
                regs.dx.setValue(half = (result >> 16));
                break;
            case 32:
                regs.eax.setValue(result);
                regs.edx.setValue(half = (result >> 32));
                break;
            default:
                throw new NotImplException();
        }
        bits.clearOCA();
        bits.setData(v1, v2, result, code, width, SZP);
        switch (code) {
            case MUL:
                bits.of.set(half != 0);
                bits.cf.set(half != 0);
                break;
            case IMUL:
                half = NumberUtils.asSigned(result, width);
                bits.of.set(half != result);
                bits.cf.set(half != result);
                break;
            default:
                throw new NotImplException();
        }
        return result;
    }

    public long imul_(long v1, long v2, int width) {
        v1 = NumberUtils.asSigned(v1, width);
        v2 = NumberUtils.asSigned(v2, width);
        long v = v1 * v2;
        bits.clearOCA();
        bits.setData(v1, v2, v, IMUL, width, SZP);
        boolean ok = v == NumberUtils.asSigned(v, width * 2);
        bits.of.set(!ok);
        bits.cf.set(!ok);
        return v;
    }

    public long div_() {
        Regs regs = pc.cpu.regs;
        long v1, v2, result1, result2;
        v2 = mrs.getValMemory(pc);
        int width = getOpWidth();
        switch (width) {
            case 8:
                v1 = regs.ax.getValue();
                break;
            case 16:
                v1 = (regs.dx.getValue() << 16) | regs.ax.getValue();
                break;
            case 32:
                v1 = (regs.edx.getValue() << 32) | regs.eax.getValue();
                break;
            case 64:
            default:
                throw new NotImplException();
        }
        result1 = v1 / v2;
        result2 = v1 % v2;
        long half;
        switch (width) {
            case 8:
                regs.al.setValue(result1);
                regs.ah.setValue(result2);
                break;
            case 16:
                regs.ax.setValue(result1);
                regs.dx.setValue(result2);
                break;
            case 32:
                regs.eax.setValue(result1);
                regs.edx.setValue(result2);
                break;
            default:
                throw new NotImplException();
        }
        return result1;
    }

    //0
    public long add_(long v1, long v2) {
        bits.setData(v1, v2, v1 + v2, ADD, getOpWidth(), OSZAPC);
        return bits.getResult();
    }

    //1
    public long or_(long v1, long v2) {
        bits.clearOCA();
        bits.setData(v1, v2, v1 | v2, OR, getOpWidth(), SZP);
        return bits.getResult();
    }

    //2
    public long adc_(long v1, long v2) {
        boolean cf = bits.cf();
        bits.setData(v1, v2, v1 + v2 + (cf ? 1 : 0), ADC, getOpWidth(), OSZAPC);
        return bits.getResult();
    }

    //3
    public long sbb_(long v1, long v2) {
        boolean cf = bits.cf();
        bits.setData(v1, v2, v1 - (v2 + (cf ? 1 : 0)), SBB, getOpWidth(), OSZAPC);
        return bits.getResult();
    }

    //4
    public long and_(long v1, long v2) {
        bits.clearOCA();
        bits.setData(v1, v2, v1 & v2, AND, getOpWidth(), SZP);
        return bits.getResult();
    }

    //5
    public long sub_(long v1, long v2) {
        bits.setData(v1, v2, v1 - v2, SUB, getOpWidth(), OSZAPC);
        return bits.getResult();
    }

    //6
    private long xor_(long v1, long v2) {
        bits.clearOCA();
        bits.setData(v1, v2, v1 ^ v2, XOR, getOpWidth(), SZP);
        return bits.getResult();
    }

    //7
    private long cmp_(long v1, long v2) {
        return sub_(v1, v2);
    }

    protected long cal(int type, long v1, long v2) {
        long v;
        switch (type) {
            case 0:
            case ADD:
                v = add_(v1, v2);
                break;
            case 1:
            case OR:
                v = or_(v1, v2);
                break;
            case 2:
            case ADC:
                v = adc_(v1, v2);
                break;
            case 3:
            case SBB:
                v = sbb_(v1, v2);
                break;
            case 4:
            case AND:
            case TEST:
            case TEST2:
                v = and_(v1, v2);
                break;
            case 5:
            case SUB:
                v = sub_(v1, v2);
                break;
            case 6:
            case XOR:
                v = xor_(v1, v2);
                break;
            case 7:
            case CMP:
                v = cmp_(v1, v2);
                break;
            default:
                throw new NotImplException();
        }
        return v;
    }

    public void execute83() {
        long v2 = readOp(8);
        v2 = NumberUtils.signExtend8(v2, mrs.opWidth);
        long v1 = mrs.getValMemory(pc);
        long v = cal(mrs.regIndex, v1, v2);
        if (mrs.regIndex != 7) {
            mrs.setValMemory(pc, v);
        }
    }

    public void executeIncReg() {
        int r = getOpcode() - 0x40;
        BaseReg reg = getReg(pc, mrs.parseReg(this, executor.getBits(), r));
        reg.setValue(inc_(reg.getValue()));
    }

    public void executeDecReg() {
        int r = getOpcode() - 0x48;
        BaseReg reg = getReg(pc, mrs.parseReg(this, executor.getBits(), r));
        reg.setValue(dec_(reg.getValue()));
    }

    public void executeIncRm() {
        long val = mrs.getValMemory(pc);
        mrs.setValMemory(pc, inc_(val));
    }

    public void executeDecRm() {
        long val = mrs.getValMemory(pc);
        mrs.setValMemory(pc, dec_(val));
    }

    public void executeCal1(int type, boolean rm) {
        long v1 = mrs.getValMemory(pc);
        long v2 = mrs.getValReg(pc);
        long v;
        if (rm) {
            v = cal(type, v2, v1);
        } else {
            v = cal(type, v1, v2);
        }
        if (type != CMP && type != TEST) {
            if (rm) {
                mrs.setValReg(pc, v);
            } else {
                mrs.setValMemory(pc, v);
            }
        }
    }

    public void executeCal2(int type) {
        read1();
        long v = cal(type, __reg.getValue(), __v1);
        if (type != CMP && type != TEST) {
            __reg.setValue(v);
        }
    }

    public void executeCal3() {
        read0();
        long v = mrs.getValMemory(pc);
        v = cal(mrs.regIndex, v, __v1);
        if (mrs.regIndex != 7) {
            mrs.setValMemory(pc, v);
        }
    }

    public void executeCbw() {
        int width = executor.getBits();
        BaseReg reg =pc.cpu.regs.ax;
        BaseReg to=reg.getRegWithWidth(width);
        width/=2;
        BaseReg from = reg.getRegWithWidth(width);
        to.setValue(NumberUtils.signExtend(from.getValue(),width,to.getWidth()));
    }

    public void executeCwd() {
        int width=executor.getBits();
        BaseReg reg1=pc.cpu.regs.ax.getRegWithWidth(width);
        BaseReg reg2=pc.cpu.regs.dx.getRegWithWidth(width);
        if(NumberUtils.hasSign(reg1.getValue(),width)) {
            reg2.setValue(0);
        } else {
            reg2.setValue(0xffffffffffffffffL);
        }
    }

    public long bitsOp(long v, long c) {
        assert c >= 0;
        int oper = BITS_BASE + mrs.regIndex;
        int opWidth = getOpWidth();
        switch (oper) {
            case SHL:
            case SAL:
                bits.setData(v, c, v << c, oper, getOpWidth(), OSZAPC);
                break;
            case SHR:
                bits.setData(v, c, v >> c, oper, getOpWidth(), OSZAPC);
                break;
            case SAR:
                v = NumberUtils.asSigned(v, opWidth);
                bits.setData(v, c, v >>> c, oper, getOpWidth(), OSZAPC);
                break;
            default:
                throw new NotImplException();
        }
        return bits.getResult();
    }

    public void bitsOps(long c) {
        if (c == -1) {
            c = input.read();
        } else if (c == -2) {
            c = pc.cpu.regs.cl.getValue();
        }
        long v = mrs.getValMemory(pc);
        v = bitsOp(v, c);
        mrs.setValMemory(pc, v);
    }

    public void executeCal4() {
        if (mrs.regIndex == 0) {
            read0(); //test
        }
        int oper = CAL2_BASE + mrs.regIndex;
        switch (oper) {
            case TEST:
            case TEST2:
                and_(mrs.getValMemory(pc), __v1);
                break;
            case MUL:
            case IMUL:
                mul_(oper);
                break;
            case DIV:
                div_();
                break;
            case NEG:
                long v1 = mrs.getValMemory(pc);
                long v2 = -NumberUtils.asSigned(v1, getOpWidth());
                mrs.setValMemory(pc, v2);
                bits.setData(v1, getOpcode(), v2, oper, getOpWidth(), OSZAPC);
                break;
            case NOT:
                mrs.setValMemory(pc, ~mrs.getValMemory(pc));
                break;
            default:
                throw new NotImplException();
        }
    }

    public void executeIMUL1() {
        int width = getOpWidth();
        long v = imul_(mrs.getValMemory(pc), mrs.getValReg(pc), width);
        mrs.setValReg(pc, v);
    }

    public void executeIMUL2() {
        read0();
        long v = imul_(mrs.getValMemory(pc), __v1, __width);
        mrs.setValReg(pc, v);
    }

    public void executeIMUL3() {
        long v2 = NumberUtils.asSigned(readOp(8), 8);
        long v = imul_(mrs.getValMemory(pc), v2, getOpWidth());
        mrs.setValReg(pc, v);
    }

    private long getSHRLD(boolean cl, boolean shl) {
        long count;
        if (cl) {
            count = bits.regs.cl.getValue();
        } else {
            count = readOp(8);
        }
        int width = getOpWidth();
        if (width == 64) {
            count = count % width;
        } else {
            count = count % 32;
        }
        __v2 = count;
        __v3 = 0;
        if (count > 0 && count <= width) {
            long v1 = mrs.getValMemory(pc);
            __v1 = v1;
            long v2 = mrs.getValReg(pc);
            if (shl) {
                v1 = v1 << count;
                v2 = v2 >> (width - count);
            } else {
                v1 = v1 >> count;
                v2 = v2 << (width - count);
            }
            bits.setData(__v1, __v2, v1 | v2, shl ? SHLD : SHRD, width, OSZAPC);
            __v3 = 1;
        }
        return bits.getResult();
    }

    public void executeSHRD(boolean cl) {
        long v = getSHRLD(cl, false);
        if (__v3 == 1) {
            mrs.setValMemory(pc, v);
        }
    }

    public void executeSHLD(boolean cl) {
        long v = getSHRLD(cl, true);
        if (__v3 == 1) {
            mrs.setValMemory(pc, v);
        }
    }

    public void executeScas() {
        Regs regs = pc.cpu.regs;
        Segment seg1 = regs.es;
        int addressWidth = getAddressWidth(executor.getBits());
        BaseReg off1 = regs.di.getRegWithWidth(addressWidth);
        long address1 = seg1.getAddress(off1);

        int opWidth = getOpWidth();
        int n = opWidth / 8;
        assert n * 8 == opWidth;
        boolean df = bits.df.get();
        BaseReg reg = regs.ax.getRegWithWidth(opWidth);

        long v=0;
        long v2 = reg.getValue();
        if (isHasf3() || isHasf2()) {
            BaseReg cr = regs.cx.getRegWithWidth(getAddressWidth(executor.getBits()));
            long c = cr.getValue();
            int k = 0;
            while (c != 0) {
                executor.checkIR(pc,false);
                v = mrs.memoryRead(pc, address1, opWidth);
                address1 = df ? address1 - n : address1 + n;
                c = c - 1;
                k++;
                if (isHasf3()) {
                    if (v != v2) {
                        break;
                    }
                }
                if (isHasf2()) {
                    if (v == v2) {
                        break;
                    }
                }
            }
            cr.setValue(c);
            off1.setValue(df ? (off1.getValue() - n * k) : (off1.getValue() + n * k));
            if(k>0) {
                cmp_(v2,v);
            }
        } else {
            v = mrs.memoryRead(pc, address1, opWidth);
            cmp_(v2, v);
            off1.setValue(df ? (off1.getValue() - n) : (off1.getValue() + n));
        }
    }

}
