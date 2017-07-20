package com.zms.zpc.support;

import com.zms.zpc.debugger.*;
import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.processor.*;
import com.zms.zpc.emulator.reg.*;
import com.zms.zpc.execute.*;

import java.io.*;

/**
 * Created by 张小美 on 17/七月/1.
 * Copyright 2002-2016
 */
public class Warnings {

    public static void main(String[] args) throws Exception {
        System.out.println(GarUtils.dump(new ByteArrayInputStream(null), new ByteArrayOutputStream()));
        new CodeStream().readFully(new byte[10], 2, 3);
        IDEFrame ide = new IDEFrame(new ZPC());
        ide.showNew("a", "b", false);
        System.out.println(ide.select(null));
        InstructionExecutor is=new InstructionExecutor();
        ModRMSIB mod = new ModRMSIB(is);
        System.out.println(mod.setValMemory(new PC(), 0L));
        System.out.println(mod.setValReg(new PC(), 2));
        Processor cpu=new Processor(new ProcessorConfig());
        System.out.println(new BaseReg_0("a",new Regs(cpu),1,1).setValue(123));

        PC pc = new PC();
        is.executePush50(new CodeExecutor(),new CodeStream(), pc,123);
        is.executeIF_(new CodeExecutor(),new CodeStream(),new PC(),true);
        is.executeDF_(new CodeExecutor(),new CodeStream(),new PC(),true);
        Segment seg= (Segment) pc.cpu.regs.getReg("DS");
        seg.setValue16(12,false);
    }

}
