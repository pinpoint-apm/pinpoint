package com.nhn.pinpoint.thrift.io;

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
            } catch (IOException e) {
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