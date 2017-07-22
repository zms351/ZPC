package com.zms.zpc.execute;

import com.zms.zpc.emulator.reg.BaseReg;
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

    public void executeIncRm() {
        long val = mrs.getValMemory(pc);
        mrs.setValMemory(pc, inc_(val));
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
        if(type!=CMP) {
            mrs.setValReg(pc, v);
        }
    }

    public void executeCal2(int type) {
        read1();
        long v = cal(type, __reg.getValue(), __v1);
        if(type!=CMP) {
            __reg.setValue(v);
        }
    }

    public void executeCmp82() {
        read0();
        long v = mrs.getValMemory(pc);
        cmp_(v, __v1);
    }

}
