package com.nhn.pinpoint.thrift.io;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        ByteArrayOutputStreamTransport transport = new ByteArrayOutputStreamTransport(new ByteArrayOutputStream());

        UnsafeByteArrayOutputStream out = new UnsafeByteArrayOutputStream();
        out.write(buf);

        List<TBaseStreamNode> nodes01 = new ArrayList<TBaseStreamNode>();
        TBaseStreamNode node01 = new TBaseStreamNode(out);
        node01.setBeginPosition(0);
        node01.setEndPosition(1);

        nodes01.add(node01);

        List<TBaseStreamNode> nodes02 = new ArrayList<TBaseStreamNode>();
        TBaseStreamNode node02 = new TBaseStreamNode(out);
        node02.setBeginPosition(1);
        node02.setEndPosition(2);

        nodes02.add(node02);

        ReplaceListCompactProtocol protocol01 = new ReplaceListCompactProtocol(transport);
        protocol01.addReplaceField("spanEventList", nodes01);
        span.write(protocol01);

        System.out.println("size: " + transport.getBufferPosition());

        ReplaceListCompactProtocol protocol02 = new ReplaceListCompactProtocol(transport);
        protocol02.addReplaceField("spanEventList", nodes02);
        span.write(protocol02);

        System.out.println("size: " + transport.getBufferPosition());
    }
}