package com.zms.zpc.support;

import com.zms.zpc.debugger.*;
import com.zms.zpc.emulator.PC;
import com.zms.zpc.emulator.processor.Regs;
import com.zms.zpc.emulator.reg.BaseReg_0;
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
        ModRMSIB mod = new ModRMSIB();
        System.out.println(mod.setValMemory(new PC(), 0L));
        System.out.println(mod.setValReg(new PC(), 2));
        System.out.println(new BaseReg_0("a",new Regs(),1,1).setValue(123));

        InstructionExecutor is=new InstructionExecutor();
        is.executePush50(new CodeExecutor(),new CodeStream(),new PC(),123);
        is.executeIF_(new CodeExecutor(),new CodeStream(),new PC(),true);
        is.executeDF_(new CodeExecutor(),new CodeStream(),new PC(),true);
    }

}
