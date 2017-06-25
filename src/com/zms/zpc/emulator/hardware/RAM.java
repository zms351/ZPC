package com.zms.zpc.emulator.hardware;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public abstract class RAM {

    public abstract int read(long context,long pos);

    public abstract int read(long context,long pos,byte[] bytes,int offset,int size);

    public abstract void write(long context,long pos,int v);

    public abstract int write(long context,long pos,byte[] bytes,int offset,int size);

}
