package com.zms.zpc.emulator.assembler;

import com.zms.zpc.support.GarUtils;

/**
 * Created by 张小美 on 17/六月/21.
 * Copyright 2002-2016
 */
public class Disassembler {

    public static synchronized String nativeAssemble(byte[] bytes, int bits) {
        try {
            Assembler.initNative();
            GarUtils.saveFile(Assembler.tempNativeInput, bytes);
            ProcessBuilder pb = new ProcessBuilder(Assembler.Ndisasm.getPath(), "-b", String.valueOf(bits), Assembler.tempNativeInput.getPath());
            pb.redirectErrorStream(true);
            pb.directory(Assembler.Ndisasm.getParentFile());
            pb.redirectOutput(Assembler.tempNativeOutput);
            int code = pb.start().waitFor();
            return GarUtils.loadFile(Assembler.tempNativeOutput);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
