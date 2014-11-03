package com.nhn.pinpoint.thrift.io;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.apache.thrift.transport.TTransportException;
import org.junit.Before;
import org.junit.Test;

public class ByteArrayOutputStreamTransportTest {
    private ByteArrayOutputStream out;
    private ByteArrayOutputStreamTransport transport;
    private byte[] buf = "foo".getBytes();

    @Before
    public void before() {
        out = new ByteArrayOutputStream();
        transport = new ByteArrayOutputStreamTransport(out);
    }

    @Test
    public void write() throws TTransportException {
        transport.write(buf, 0, buf.length);
        assertTrue(Arrays.equals(buf, out.toByteArray()));
        assertTrue(Arrays.equals(buf, transport.getBuffer()));

        transport.write(buf);
        assertEquals(out.size(), transport.getBufferPosition());

        assertEquals(-1, transport.getBytesRemainingInBuffer());

        transport.flush();
        assertEquals(0, out.size());

        // unsupported operation
        try {
            fail("passed unsupported operation");
        } catch (Exception e) {
        }

        try {
            transport.consumeBuffer(1);
            fail("passed unsupported operation");
        } catch (Exception e) {
        }
    }

    @Test(expected = TTransportException.class)
    public void read() throws TTransportException {
        transport.read(buf, 0, 1);
    }
    
    

}