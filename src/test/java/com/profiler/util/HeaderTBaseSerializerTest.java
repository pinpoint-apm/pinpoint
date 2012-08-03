package com.profiler.util;

import com.profiler.dto.Header;
import com.profiler.dto.JVMInfoThriftDTO;
import org.apache.thrift.TBase;
import org.junit.Test;

import java.util.Arrays;
import java.util.logging.Logger;

public class HeaderTBaseSerializerTest {
    private final Logger logger = Logger.getLogger(HeaderTBaseSerializerTest.class.getName());


    @Test
    public void testSerialize() throws Exception {
        HeaderTBaseSerializer serializer = new HeaderTBaseSerializer();

        Header header = new Header();
        // 10 을 JVMInfoThriftDTO type
        header.setType((short) 10);

        JVMInfoThriftDTO jvmInfoThriftDTO = new JVMInfoThriftDTO();
        byte[] serialize = serializer.serialize(header, jvmInfoThriftDTO);
        dump(serialize);

        HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializer();
        TBaseLocator locator = new DefaultTBaseLocator();
        TBase deserialize = deserializer.deserialize(locator, serialize);
        logger.info("deserialize:" + deserialize.getClass());
    }

    public void dump(byte[] data) {
        String s = Arrays.toString(data);
        logger.info(data.getClass().getName()+ ":" + s);
    }
}
