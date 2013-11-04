package com.nhn.pinpoint.thrift.io;

import com.nhn.pinpoint.thrift.dto.TAgentInfo;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author emeroad
 */
public class HeaderTBaseSerializerTest {
    private final Logger logger = LoggerFactory.getLogger(HeaderTBaseSerializerTest.class.getName());


    @Test
    public void testSerialize() throws Exception {
        HeaderTBaseSerializer serializer = new HeaderTBaseSerializer();

        Header header = new Header();
        // 10 을 JVMInfoThriftDTO type
        header.setType((short) 10);

        TAgentInfo tAgentInfo = new TAgentInfo();
        tAgentInfo.setAgentId("agentId");
        tAgentInfo.setHostname("host");
        tAgentInfo.setApplicationName("applicationName");

        byte[] serialize = serializer.serialize(tAgentInfo);
        dump(serialize);

        HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializer();
        TAgentInfo deserialize = (TAgentInfo) deserializer.deserialize(serialize);
        logger.debug("deserializer:{}", deserialize.getClass());

        Assert.assertEquals(deserialize, tAgentInfo);
    }

    public void dump(byte[] data) {
        String s = Arrays.toString(data);
        logger.debug("size:{} data:{}", data.length, s);
    }
}
