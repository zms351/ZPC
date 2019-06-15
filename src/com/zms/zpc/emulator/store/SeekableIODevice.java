package com.zms.zpc.emulator.store;

import java.io.IOException;

/**
 * Created by 张小美 on 2019-06-09.
 * Copyright 2002-2016
 */
public interface SeekableIODevice {

    /**
     * Move read/write offset to given location (in bytes) from the start of the
     * device.
     *
     * @param offset location to seek to
     * @throws java.io.IOException if <code>offset</code> is invalid
     */
    void seek(long offset) throws IOException;

    /**
     * Writes <code>length</code> bytes from <code>data</code> starting at
     * offset into the device.
     *
     * @param data   buffer to read data from
     * @param offset start offset in <code>data<code>
     * @param length number of bytes to write
     * @return number of bytes written
     * @throws java.io.IOException on I/O error.
     */
    int write(byte[] data, int offset, int length) throws IOException;

    /**
     * Reads <code>length</code> bytes from the device, writing into
     * <code>data</code> at <code>offset</code>.
     *
     * @param data   buffer to write data into
     * @param offset start offset in <code>data</code>
     * @param length number of bytes to read
     * @return number of bytes read
     * @throws java.io.IOException on I/O error
     */
    int read(byte[] data, int offset, int length) throws IOException;

    /**
     * Returns the length of the device.
     *
     * @return device length
     */
    long length();

    /**
     * Returns <code>true</code> if the device cannot be written to.
     *
     * @return <code>true</code> if read-only
     */
    boolean readOnly();

    /**
     * Closes and releases the resources associated with this instance.
     */
    void close() throws IOException;

    /**
     * Configure device using the given <code>String</code>.  What this object
     * chooses to do with the given <code>String</code> is implementation
     * dependant.
     *
     * @param opts configuration string
     * @throws java.io.IOException                on an I/O error configuring the device
     * @throws java.lang.IllegalArgumentException if the configuration string is invalid.
     */
    void configure(String opts) throws IOException, IllegalArgumentException;

}
