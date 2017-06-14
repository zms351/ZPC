package com.zms.zpc.emulator.assembler;

import java.io.ByteArrayOutputStream;

/**
 * Created by 张小美 on 17/六月/14.
 * Copyright 2002-2016
 */
public class AssemblerOutput {

    private AssemblerByteArrayOutputStream output;

    public AssemblerOutput() {
        output=new AssemblerByteArrayOutputStream();
    }

    public void reset() {
        output.reset();
    }

    public int getPosition() {
        return output.getCount();
    }

    private int mark;

    public void setMark(int mark) {
        this.mark=mark;
    }

    public int getMark() {
        return mark;
    }

    public void write(int b) {
        output.write(b);
    }

    public byte[] getBuffer() {
        return output.getBuffer();
    }

}

class AssemblerByteArrayOutputStream extends ByteArrayOutputStream {

    public int getCount() {
        return count;
    }

    public byte[] getBuffer() {
        return super.buf;
    }

}
