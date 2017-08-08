package com.zms.zpc.emulator.memory;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public interface RAM {

    int read(long context, long pos);

    void write(long context, long pos, int v);

}
