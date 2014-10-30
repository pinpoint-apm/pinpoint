package com.nhn.pinpoint.thrift.io;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.apache.thrift.transport.TTransportException;
import org.junit.Test;

public class ByteArrayOutputStreamTransportTest {

    @Test
    public void test() throws TTransportException {
        byte[] buf = "foo".getBytes();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStreamTransport transport = new ByteArrayOutputStreamTransport(out);

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
            transport.read(buf, 0, 1);
            fail("passed unsupported operation");
        } catch (Exception e) {
        }

        try {
            transport.consumeBuffer(1);
            fail("passed unsupported operation");
        } catch (Exception e) {
        }
    }
}
