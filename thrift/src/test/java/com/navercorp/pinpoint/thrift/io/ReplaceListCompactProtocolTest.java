package com.nhn.pinpoint.thrift.io;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.thrift.protocol.TCompactProtocol;
import org.junit.Before;
import org.junit.Test;

import com.nhn.pinpoint.thrift.dto.TSpan;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;

public class ReplaceListCompactProtocolTest {

    private TSpan span = new TSpan();
    final byte[] buf = new byte[1024];


    @Before
    public void before() {
        // add dummy span-event list
        List<TSpanEvent> spanEventList = new ArrayList<TSpanEvent>();
        spanEventList.add(new TSpanEvent());
        span.setSpanEventList(spanEventList);
        span.setSpanEventListIsSet(true);

        // init byte buffer
        Arrays.fill(buf, Byte.valueOf("0"));
    }

    @Test
    public void replace() throws Exception {
        List<ByteArrayOutput> nodes01 = new ArrayList<ByteArrayOutput>();
        final AtomicInteger writeTo01 = new AtomicInteger(0);
        nodes01.add(new ByteArrayOutput() {
            public void writeTo(OutputStream out) throws IOException {
                writeTo01.incrementAndGet();
            }
        });

        final AtomicInteger writeTo02 = new AtomicInteger(0);
        List<ByteArrayOutput> nodes02 = new ArrayList<ByteArrayOutput>();
        nodes02.add(new ByteArrayOutput() {
            public void writeTo(OutputStream out) throws IOException {
                writeTo02.incrementAndGet();
            }
        });

        ByteArrayOutputStreamTransport transport = new ByteArrayOutputStreamTransport(new ByteArrayOutputStream());
        TReplaceListProtocol protocol01 = new TReplaceListProtocol(new TCompactProtocol(transport));
        protocol01.addReplaceField("spanEventList", nodes01);
        span.write(protocol01);
        assertEquals(1, writeTo01.get());
        

        TReplaceListProtocol protocol02 = new TReplaceListProtocol(new TCompactProtocol(transport));
        protocol02.addReplaceField("spanEventList", nodes02);
        span.write(protocol02);
        assertEquals(1, writeTo02.get());
    }
}