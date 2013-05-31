package com.nhn.pinpoint.common.util;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class ByteSizeTest {
    @Test
    public void test() throws TException {
        TCompactProtocol.Factory factory = new TCompactProtocol.Factory();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
        TIOStreamTransport transport = new TIOStreamTransport(baos);
        TProtocol protocol = factory.getProtocol(transport);

        long l = TimeUnit.DAYS.toMillis(1);
        System.out.println("day:" + l);
        long currentTime = System.currentTimeMillis();
        System.out.println("currentTime:" + currentTime);
        protocol.writeI64(l);
        byte[] buffer = baos.toByteArray();
        System.out.println(buffer.length);

    }


}
