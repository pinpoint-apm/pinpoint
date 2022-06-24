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

import org.apache.thrift.transport.TTransportException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ByteArrayOutputStreamTransportTest {
    private ByteArrayOutputStream out;
    private ByteArrayOutputStreamTransport transport;
    private byte[] buf = "foo".getBytes();

    @BeforeEach
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
    }

    @Test
    public void flush() throws TTransportException {
        transport.write(buf, 0, buf.length);
        assertEquals(buf.length, out.size());

        transport.flush();
        assertEquals(0, out.size());
    }

    @Test
    public void read() throws TTransportException {
        Assertions.assertThrows(TTransportException.class, () -> {
            transport.read(buf, 0, 1);
        });
    }

    @Test
    public void consumeBuffer() throws TTransportException {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            transport.consumeBuffer(1);
        });
    }
}