package com.zms.zpc.emulator.board.helper;

import com.zms.zpc.support.BaseObj;

/**
 * readPosition 可以达到 writePosition，表示空了
 * writePosition 在初始时是等于 readPosition，之后不能追上 readPosition，如果要追上，表示满了
 * <p>
 * Created by 张小美 on 17/七月/25.
 * Copyright 2002-2016
 */
public class InputDataQueue extends BaseObj {

    private static final int KBD_QUEUE_SIZE = 256;

    private byte[] data;
    private byte[] aux;
    private int readPosition;  //本次要读的位置
    private int writePosition; //本次要写的位置
    private int Size;

    public InputDataQueue() {
        this(KBD_QUEUE_SIZE);
    }

    public InputDataQueue(int size) {
        assert size > 1;
        Size = size + 1;
        data = new byte[Size];
        aux = new byte[Size];
        reset();
    }

    public boolean isEmpty() {
        return readPosition == writePosition;
    }

    public boolean isFull() {
        return ((writePosition + 1) % Size) == readPosition;
    }

    public void writeData(byte _data, byte _aux) {
        synchronized (this) {
            if (!isFull()) {
                data[writePosition] = _data;
                aux[writePosition] = _aux;
                writePosition = (writePosition + 1) % Size;
            }
        }
    }

    public byte data_;
    public byte aux_;

    public boolean readData() {
        synchronized (this) {
            if (!isEmpty()) {
                data_ = data[readPosition];
                aux_ = aux[readPosition];
                readPosition = (readPosition + 1) % Size;
                return true;
            }
        }
        return false;
    }

    public void reset() {
        synchronized (this) {
            readPosition = writePosition = 0;
        }
    }

}
