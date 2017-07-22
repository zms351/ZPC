package com.zms.zpc.emulator.memory;

import com.zms.zpc.support.NotImplException;

/**
 * Created by 张小美 on 17/六月/25.
 * Copyright 2002-2016
 */
public abstract class RAM {

    public abstract int read(long context, long pos);

    public abstract int read(long context, long pos, byte[] bytes, int offset, int size);

    public abstract void write(long context, long pos, int v);

    public abstract int write(long context, long pos, byte[] bytes, int offset, int size);

    public long read(long context, long pos, int width) {
        switch (width) {
            case 8:
                return read(context, pos) & 0xff;
            case 16:
                return (read(context, pos) & 0xff) | ((read(context, pos + 1) & 0xff) << 8);
            case 32:
                return read(context, pos, 16) | (read(context, pos + 2, 16) << 16);
            case 64:
                return read(context, pos, 32) | (read(context, pos + 4, 32) << 32);
            default:
                throw new NotImplException();
        }
    }

    public void write(long context, long pos, long val, int width) {
        switch (width) {
            case 8:
                write(context, pos, (int) val);
                break;
            case 16:
                write(context, pos, (int) val);
                write(context, pos + 1, (int) (val >>> 8));
                break;
            case 32:
                write(context, pos, val, 16);
                write(context, pos + 2, val >>> 16, 16);
                break;
            case 64:
                write(context, pos, val, 32);
                write(context, pos + 4, val >>> 32, 32);
                break;
            default:
                throw new NotImplException();
        }
    }

}
