/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.thrift.io;

import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * @author emeroad
 */
public class HeaderTBaseSerializerTest {
    private final Logger logger = LogManager.getLogger(HeaderTBaseSerializerTest.class);


    @Test
    public void testSerialize1() throws Exception {
        HeaderTBaseSerializer serializer = new HeaderTBaseSerializerFactory().createSerializer();
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

        TAgentInfo tAgentInfo = new TAgentInfo();
        tAgentInfo.setAgentId("agentId");
        tAgentInfo.setHostname("host");
        tAgentInfo.setApplicationName("applicationName");

        byte[] serialize = serializer.serialize(tAgentInfo);
        dump(serialize);

        Message<TBase<?, ?>> message = deserializer.deserialize(serialize);
        TAgentInfo deserialize = (TAgentInfo) message.getData();
        logger.debug("deserializer:{}", deserialize.getClass());

        Assertions.assertEquals(deserialize, tAgentInfo);
    }

    public void dump(byte[] data) {
        String s = Arrays.toString(data);
        logger.trace("size:{} data:{}", data.length, s);
    }
}
