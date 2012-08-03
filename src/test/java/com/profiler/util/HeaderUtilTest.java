package com.profiler.util;

import com.profiler.dto.*;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.junit.Test;

import java.util.Arrays;
import java.util.logging.Logger;

public class HeaderUtilTest {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Test
    public void read() throws TException {


//        Header header = new Header();
//        dump(header);
//        logger.info(Byte.toString(header.getSignature()));
//
//
//        JVMInfoThriftDTO dto = new JVMInfoThriftDTO();
//        JVMInfoData data = new JVMInfoData(header, dto);
//        dump(data);
//
//        Request request = new Request(header, new RequestThriftDTO());
//        dump(request);

    }

    private void dump(TBase data) throws TException {
        TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
        byte[] serialize = serializer.serialize(data);
        String s = Arrays.toString(serialize);
        logger.info(data.getClass().getName()+ ":" + s);
    }
}
