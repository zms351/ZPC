package com.zms.zpc.emulator.processor;

import com.zms.zpc.emulator.reg.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * Created by 张小美 on 17/五月/24.
 * Copyright 2002-2016
 */
public class Regs {

    private final long[] rvs = new long[128];

    public final BaseReg rax = new Reg("rax", this, 0, 64);
    public final BaseReg eax = new Reg("eax", this, 0, 32);
    public final BaseReg ax = new Reg("ax", this, 0, 16);
    public final BaseReg ah = new Reg8("ah", this, 0);
    public final BaseReg al = new Reg("al", this, 0, 8);

    public final BaseReg rbx = new Reg("rbx", this, 1, 64);
    public final BaseReg ebx = new Reg("ebx", this, 1, 32);
    public final BaseReg bx = new Reg("bx", this, 1, 16);
    public final BaseReg bh = new Reg8("bh", this, 1);
    public final BaseReg bl = new Reg("bl", this, 1, 8);

    public final BaseReg rcx = new Reg("rcx", this, 2, 64);
    public final BaseReg ecx = new Reg("ecx", this, 2, 32);
    public final BaseReg cx = new Reg("cx", this, 2, 16);
    public final BaseReg ch = new Reg8("ch", this, 2);
    public final BaseReg cl = new Reg("cl", this, 2, 8);

    public final BaseReg rdx = new Reg("rdx", this, 3, 64);
    public final BaseReg edx = new Reg("edx", this, 3, 32);
    public final BaseReg dx = new Reg("dx", this, 3, 16);
    public final BaseReg dh = new Reg8("dh", this, 3);
    public final BaseReg dl = new Reg("dl", this, 3, 8);

    public final BaseReg rsi = new Reg("rsi", this, 4, 64);
    public final BaseReg esi = new Reg("esi", this, 4, 32);
    public final BaseReg si = new Reg("si", this, 4, 16);
    public final BaseReg sil = new Reg("sil", this, 4, 8);

    public final BaseReg rdi = new Reg("rdi", this, 5, 64);
    public final BaseReg edi = new Reg("edi", this, 5, 32);
    public final BaseReg di = new Reg("di", this, 5, 16);
    public final BaseReg dil = new Reg("dil", this, 5, 8);

    public final BaseReg rbp = new Reg("rbp", this, 6, 64);
    public final BaseReg ebp = new Reg("ebp", this, 6, 32);
    public final BaseReg bp = new Reg("bp", this, 6, 16);
    public final BaseReg bpl = new Reg("bpl", this, 6, 8);

    public final BaseReg rsp = new Reg("rsp", this, 7, 64);
    public final BaseReg esp = new Reg("esp", this, 7, 32);
    public final BaseReg sp = new Reg("sp", this, 7, 16);
    public final BaseReg spl = new Reg("spl", this, 7, 8);

    public final BaseReg r8 = new Reg("r8", this, 8, 64);
    public final BaseReg r8d = new Reg("r8d", this, 8, 32);
    public final BaseReg r8w = new Reg("r8w", this, 8, 16);
    public final BaseReg r8b = new Reg("r8b", this, 8, 8);

    public final BaseReg r9 = new Reg("r9", this, 9, 64);
    public final BaseReg r9d = new Reg("r9d", this, 9, 32);
    public final BaseReg r9w = new Reg("r9w", this, 9, 16);
    public final BaseReg r9b = new Reg("r9b", this, 9, 8);

    public final BaseReg r10 = new Reg("r10", this, 10, 64);
    public final BaseReg r10d = new Reg("r10d", this, 10, 32);
    public final BaseReg r10w = new Reg("r10w", this, 10, 16);
    public final BaseReg r10b = new Reg("r10b", this, 10, 8);

    public final BaseReg r11 = new Reg("r11", this, 11, 64);
    public final BaseReg r11d = new Reg("r11d", this, 11, 32);
    public final BaseReg r11w = new Reg("r11w", this, 11, 16);
    public final BaseReg r11b = new Reg("r11b", this, 11, 8);

    public final BaseReg r12 = new Reg("r12", this, 12, 64);
    public final BaseReg r12d = new Reg("r12d", this, 12, 32);
    public final BaseReg r12w = new Reg("r12w", this, 12, 16);
    public final BaseReg r12b = new Reg("r12b", this, 12, 8);

    public final BaseReg r13 = new Reg("r13", this, 13, 64);
    public final BaseReg r13d = new Reg("r13d", this, 13, 32);
    public final BaseReg r13w = new Reg("r13w", this, 13, 16);
    public final BaseReg r13b = new Reg("r13b", this, 13, 8);

    public final BaseReg r14 = new Reg("r14", this, 14, 64);
    public final BaseReg r14d = new Reg("r14d", this, 14, 32);
    public final BaseReg r14w = new Reg("r14w", this, 14, 16);
    public final BaseReg r14b = new Reg("r14b", this, 14, 8);

    public final BaseReg r15 = new Reg("r15", this, 15, 64);
    public final BaseReg r15d = new Reg("r15d", this, 15, 32);
    public final BaseReg r15w = new Reg("r15w", this, 15, 16);
    public final BaseReg r15b = new Reg("r15b", this, 15, 8);

    public final BaseReg rip = new Reg("rip", this, 61, 64);
    public final BaseReg eip = new Reg("eip", this, 61, 32);
    public final BaseReg ip = new Reg("ip", this, 61, 16);
    public final BaseReg ipl = new Reg("ipl", this, 61, 8);

    public final Flags rflags = new Flags("rflags", this, 62, 64);
    public final Flags eflags = new Flags("eflags", this, 62, 32);

    public final ControlReg cr0 = new ControlReg("cr0", this, 80, 64);
    public final ControlReg cr2 = new ControlReg("cr2", this, 82, 64);
    public final ControlReg cr3 = new ControlReg("cr3", this, 83, 64);
    public final ControlReg cr4 = new ControlReg("cr4", this, 84, 64);
    public final ControlReg cr8 = new ControlReg("cr8", this, 88, 64);

    //ES、CS、SS、DS、FS和GS

    public final Segment cs = new Segment("cs", this, 101);
    public final Segment ds = new Segment("ds", this, 102);
    public final Segment es = new Segment("es", this, 103);
    public final Segment fs = new Segment("fs", this, 104);
    public final Segment gs = new Segment("gs", this, 105);
    public final Segment ss = new Segment("ss", this, 106);
    // 107--112  for bases

    public final BaseReg[] rootRegs = new BaseReg[]{
            rax,
            rbx,
            rcx,
            rdx,
            rflags,
            cs, rip,
            ss, rsp,
            ds, rsi,
            es, rdi,
            fs, rbp
    };

    public long[] getRvs() {
        return rvs;
    }

    public BaseReg[] getRootRegs() {
        return rootRegs;
    }

    public Bits bits = new Bits(this);

    public Regs() {
        this.init();
    }

    public Map<String, BaseReg> regMap1;
    public Map<Integer,BaseReg[]> regMap2;

    public BaseReg getReg(String name) {
        return regMap1.get(name.toUpperCase());
    }

    public BaseReg getRegWithWidth(Integer index,int width) {
        int i=width/8;
        assert i*8==width;
        BaseReg[] regs = regMap2.get(index);
        return regs==null?null:regs[i];
    }

    private void init() {
        regMap1 = new HashMap<>();
        Field[] fields = this.getClass().getFields();
        try {
            for (Field field : fields) {
                if (BaseReg.class.isAssignableFrom(field.getType())) {
                    int modifiers = field.getModifiers();
                    if (Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers) && (!Modifier.isStatic(modifiers))) {
                        BaseReg reg = (BaseReg) field.get(this);
                        if (reg != null) {
                            String name = reg.getName().toUpperCase().trim();
                            assert !regMap1.containsKey(name);
                            regMap1.put(name, reg);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        regMap2=new HashMap<>();
        for (BaseReg reg : regMap1.values()) {
            if(reg.getPos()==0) {
                Integer index= reg.getIndex();
                BaseReg[] regs = regMap2.computeIfAbsent(index, k -> new BaseReg[4]);
                int width = reg.getWidth();
                int i=width/8;
                assert i*8==width;
                assert regs[i]==null;
                regs[i]=reg;
            }
        }
    }

}
