package com.navercorp.pinpoint.thrift.io;

import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * Caution. not thread safe
 *
 * @Author Taejin Koo
 */
public class TOutputStreamTransport extends TTransport {

    private OutputStream outputStream;

    @Override
    public boolean isOpen() {
        return outputStream != null;
    }

    @Override
    public void open() throws TTransportException {
    }

    public void open(OutputStream outputStream) throws TTransportException {
        this.outputStream = outputStream;
    }

    @Override
    public void close() {
        this.outputStream = null;
    }

    @Override
    public int read(byte[] bytes, int index, int length) throws TTransportException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(byte[] bytes, int index, int length) throws TTransportException {
        if (this.outputStream == null) {
            throw new TTransportException(1, "TOutputStreamTransport is not opend.");
        } else {
            try {
                this.outputStream.write(bytes, index, length);
            } catch (IOException var5) {
                throw new TTransportException(0, var5);
            }
        }
    }

}
