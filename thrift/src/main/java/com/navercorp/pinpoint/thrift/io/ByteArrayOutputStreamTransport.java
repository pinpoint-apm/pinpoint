/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.thrift.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * ByteArrayOutputStreamTransport
 * - write only
 * 
 * @author jaehong.kim
 */
public class ByteArrayOutputStreamTransport extends TTransport {

    private final ByteArrayOutputStream out;

    public ByteArrayOutputStreamTransport(final ByteArrayOutputStream out) {
        this.out = out;
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return out;
    }

    @Override
    public void close() {
        if (out != null) {
            try {
                out.close();
            } catch (IOException ignore) {
                // skip
            }
        }
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void open() throws TTransportException {
    }

    @Override
    public int read(byte[] buf, int off, int len) throws TTransportException {
        throw new TTransportException(TTransportException.NOT_OPEN, "unsupported inputStream");
    }

    @Override
    public void write(byte[] buf, int off, int len) throws TTransportException {
        if (out == null) {
            throw new TTransportException(TTransportException.NOT_OPEN, "cannot write to null outputStream");
        }

        out.write(buf, off, len);
    }

    @Override
    public void flush() throws TTransportException {
        out.reset();
    }

    @Override
    public byte[] getBuffer() {
        return out.toByteArray();
    }

    @Override
    public int getBufferPosition() {
        return out.size();
    }

    @Override
    public int getBytesRemainingInBuffer() {
        return -1;
    }

    @Override
    public void consumeBuffer(int len) {
        throw new UnsupportedOperationException("unsupported ByteArrayOutputStream operation");
    }
}