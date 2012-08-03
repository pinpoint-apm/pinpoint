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
        // 10 을 JVMInfoThriftDTO type이라고 가정하자.
        header.setType((short) 10);
        byte[] serialize = serializer.serialize(header, new JVMInfoThriftDTO());
        dump(serialize);

        HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializer();
        TBaseSelector tBaseSelector = new TBaseSelector() {
            @Override
            public TBase getSelect(Header header) {
                if(header.getType() == 10) {
                    return new JVMInfoThriftDTO();
                }
                throw new IllegalArgumentException("type not found:" + header.getType());
            }
        };
        TBase deserialize = deserializer.deserialize(tBaseSelector, serialize);
        logger.info("deserialize:" + deserialize.getClass());
    }

    public void dump(byte[] data) {
        String s = Arrays.toString(data);
        logger.info(data.getClass().getName()+ ":" + s);
    }
}
