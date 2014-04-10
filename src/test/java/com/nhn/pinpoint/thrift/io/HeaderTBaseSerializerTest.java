package com.nhn.pinpoint.thrift.io;

import com.nhn.pinpoint.thrift.dto.TAgentInfo;

import org.apache.thrift.TException;
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
    public void testSerialize1() throws Exception {
    	HeaderTBaseSerializer serializer = HeaderTBaseSerDesFactory.getSerializer(false, HeaderTBaseSerDesFactory.DEFAULT_SAFETY_NOT_GURANTEED_MAX_SERIALIZE_DATA_SIZE);
    	HeaderTBaseDeserializer deserializer = HeaderTBaseSerDesFactory.getDeserializer();
    	
    	test(serializer, deserializer);
    }
    
    @Test
    public void testSerialize2() throws Exception {
    	HeaderTBaseSerializer serializer = HeaderTBaseSerDesFactory.getSerializer(true, HeaderTBaseSerDesFactory.DEFAULT_SAFETY_GURANTEED_MAX_SERIALIZE_DATA_SIZE);
    	HeaderTBaseDeserializer deserializer = HeaderTBaseSerDesFactory.getDeserializer();
    	
    	test(serializer, deserializer);
    }
    
    private void test(HeaderTBaseSerializer serializer, HeaderTBaseDeserializer deserializer) throws TException {

        Header header = new Header();
        // 10 을 JVMInfoThriftDTO type
        header.setType((short) 10);

        TAgentInfo tAgentInfo = new TAgentInfo();
        tAgentInfo.setAgentId("agentId");
        tAgentInfo.setHostname("host");
        tAgentInfo.setApplicationName("applicationName");

        byte[] serialize = serializer.serialize(tAgentInfo);
        dump(serialize);

        TAgentInfo deserialize = (TAgentInfo) deserializer.deserialize(serialize);
        logger.debug("deserializer:{}", deserialize.getClass());

        Assert.assertEquals(deserialize, tAgentInfo);
    }

    public void dump(byte[] data) {
        String s = Arrays.toString(data);
        logger.debug("size:{} data:{}", data.length, s);
    }
}
