package com.nhn.pinpoint.profiler.util;

import com.nhn.pinpoint.common.dto2.Header;
import com.nhn.pinpoint.common.dto2.thrift.JVMInfoThriftDTO;
import com.nhn.pinpoint.common.io.DefaultTBaseLocator;
import com.nhn.pinpoint.common.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.common.io.HeaderTBaseSerializer;
import com.nhn.pinpoint.common.io.TBaseLocator;
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
        byte[] serialize = serializer.serialize(jvmInfoThriftDTO);
        dump(serialize);

        HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializer();
        TBaseLocator locator = new DefaultTBaseLocator();
        JVMInfoThriftDTO deserialize = (JVMInfoThriftDTO) deserializer.deserialize(serialize);
        logger.info("deserialize:" + deserialize.getClass());

        Assert.assertEquals(deserialize.getActiveThreadCount(), activeThreadount);
        Assert.assertEquals(deserialize.getAgentId(), agentId);
    }

    public void dump(byte[] data) {
        String s = Arrays.toString(data);
        logger.info("size:"+ data.length + " data:" + s);
    }
}
