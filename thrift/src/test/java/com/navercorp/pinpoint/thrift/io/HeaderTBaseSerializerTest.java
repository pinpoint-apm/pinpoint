/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.thrift.io;

import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
import com.navercorp.pinpoint.thrift.io.Header;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;

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
        HeaderTBaseSerializer serializer = new HeaderTBaseSerializerFactory(false).createSerializer();
        HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializerFactory().createDeserializer();

        test(serializer, deserializer);
    }
    
    @Test
    public void testSerialize2() throws Exception {
        HeaderTBaseSerializer serializer = new HeaderTBaseSerializerFactory().createSerializer();
        HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializerFactory().createDeserializer();

        test(serializer, deserializer);
    }
    
    private void test(HeaderTBaseSerializer serializer, HeaderTBaseDeserializer deserializer) throws TException {

        Header header = new Header();
        // 10 is JVMInfoThriftDTO type
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
