package com.zms.zpc.emulator.memory;

import com.zms.zpc.support.*;

/**
 * Created by 张小美 on 17/八月/9.
 * Copyright 2002-2016
 */
public abstract class Memory extends BaseObj implements RAM {

    public abstract long getTotalSize();

    public abstract int read(long context, long pos, byte[] bytes, int offset, int size);

    public abstract int write(long context, long pos, byte[] bytes, int offset, int size);

    public void copyContentsIntoArray(long address, byte[] buffer, int off, int len) {
        int read=0;
        int n;
        while(read<len) {
            n=read(111,address+read,buffer,off+read,len-read);
            assert n>=0;
            read+=n;
        }
    }

    public void copyArrayIntoContents(int address, byte[] buffer, int off, int len) {
        int write=0;
        int n;
        while(write<len) {
            n=write(111,address+write,buffer,off+write,len-write);
            assert n>=0;
            write+=n;
        }
    }

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
