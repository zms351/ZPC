package com.zms.zpc.emulator.board;

/**
 * Created by 张小美 on 17/七月/2.
 * Copyright 2002-2016
 */
public interface IODevice {

    void write(int address,long v,int width);

    long read(int address,int width);

    void reset();

}
