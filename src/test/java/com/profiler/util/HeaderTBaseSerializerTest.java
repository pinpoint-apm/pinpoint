package com.profiler.util;

import com.profiler.common.dto.Header;
import com.profiler.common.dto.thrift.JVMInfoThriftDTO;
import com.profiler.common.util.DefaultTBaseLocator;
import com.profiler.common.util.HeaderTBaseDeserializer;
import com.profiler.common.util.HeaderTBaseSerializer;
import com.profiler.common.util.TBaseLocator;
import org.apache.thrift.TBase;
import org.junit.Assert;
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
        int activeThreadount = 10;
        jvmInfoThriftDTO.setActiveThreadCount(activeThreadount);
        int agentHashCde = 123;
        jvmInfoThriftDTO.setAgentHashCode(agentHashCde);
        byte[] serialize = serializer.serialize(header, jvmInfoThriftDTO);
        dump(serialize);

        HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializer();
        TBaseLocator locator = new DefaultTBaseLocator();
        JVMInfoThriftDTO deserialize = (JVMInfoThriftDTO) deserializer.deserialize(locator, serialize);
        logger.info("deserialize:" + deserialize.getClass());

        Assert.assertEquals(deserialize.getActiveThreadCount(), activeThreadount);
        Assert.assertEquals(deserialize.getAgentHashCode(), agentHashCde);
    }

    public void dump(byte[] data) {
        String s = Arrays.toString(data);
        logger.info("size:"+ data.length + " data:" + s);
    }
}
