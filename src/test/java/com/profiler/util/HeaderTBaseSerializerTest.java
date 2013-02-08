package com.profiler.util;

import com.profiler.common.dto.Header;
import com.profiler.common.dto.thrift.JVMInfoThriftDTO;
import com.profiler.common.io.DefaultTBaseLocator;
import com.profiler.common.io.HeaderTBaseDeserializer;
import com.profiler.common.io.HeaderTBaseSerializer;
import com.profiler.common.io.TBaseLocator;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class HeaderTBaseSerializerTest {
    private final Logger logger = LoggerFactory.getLogger(HeaderTBaseSerializerTest.class.getName());


    @Test
    public void testSerialize() throws Exception {
        HeaderTBaseSerializer serializer = new HeaderTBaseSerializer();

        Header header = new Header();
        // 10 을 JVMInfoThriftDTO type
        header.setType((short) 10);

        JVMInfoThriftDTO jvmInfoThriftDTO = new JVMInfoThriftDTO();
        int activeThreadount = 10;
        jvmInfoThriftDTO.setActiveThreadCount(activeThreadount);
        String agentId = "agentId";
        jvmInfoThriftDTO.setAgentId(agentId);
        byte[] serialize = serializer.serialize(header, jvmInfoThriftDTO);
        dump(serialize);

        HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializer();
        TBaseLocator locator = new DefaultTBaseLocator();
        JVMInfoThriftDTO deserialize = (JVMInfoThriftDTO) deserializer.deserialize(locator, serialize);
        logger.info("deserialize:" + deserialize.getClass());

        Assert.assertEquals(deserialize.getActiveThreadCount(), activeThreadount);
        Assert.assertEquals(deserialize.getAgentId(), agentId);
    }

    public void dump(byte[] data) {
        String s = Arrays.toString(data);
        logger.info("size:"+ data.length + " data:" + s);
    }
}
