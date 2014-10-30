package com.nhn.pinpoint.thrift.io;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.junit.Test;

public class TBaseStreamNodeTest {

    @Test
    public void size() throws Exception {
        final byte[] buf = "foo".getBytes();

        UnsafeByteArrayOutputStream out = new UnsafeByteArrayOutputStream();
        out.write(buf);
        TBaseStreamNode node = new TBaseStreamNode(out);

        node.setBeginPosition(0);
        node.setEndPosition(buf.length);

        assertEquals(buf.length, node.size());

        ByteArrayOutputStream copy = new ByteArrayOutputStream();
        node.writeTo(copy);

        Arrays.equals(buf, copy.toByteArray());
    }
}
