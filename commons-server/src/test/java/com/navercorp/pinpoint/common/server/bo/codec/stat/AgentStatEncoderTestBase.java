/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.codec.stat;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author HyunGil Jeong
 */
public abstract class AgentStatEncoderTestBase<T extends AgentStatDataPoint> {

    private static final String AGENT_ID = "testAgentId";
    private static final Random RANDOM = new Random();

    private List<AgentStatEncoder<T>> encoders;

    private AgentStatDecoder<T> decoder;

    @Autowired
    private List<AgentStatCodec<T>> codecs;

    @Before
    public void setUp() {
        this.encoders = new ArrayList<>(codecs.size());
        for (AgentStatCodec<T> codec : codecs) {
            AgentStatEncoder<T> encoder = new AgentStatEncoder<>(codec);
            encoders.add(encoder);
        }
        this.decoder = new AgentStatDecoder<>(codecs);
    }

    protected abstract List<T> createAgentStats(String agentId, long initialTimestamp, int numStats);

    @Test
    public void stats_should_be_encoded_and_decoded_into_same_value() {
        long initialTimestamp = System.currentTimeMillis();
        int numStats = RANDOM.nextInt(20) + 1;
        List<T> expectedAgentStats = this.createAgentStats(AGENT_ID, initialTimestamp, numStats);
        for (AgentStatEncoder<T> encoder : this.encoders) {
            long baseTimestamp = AgentStatUtils.getBaseTimestamp(initialTimestamp);
            long timestampDelta = initialTimestamp - baseTimestamp;
            ByteBuffer qualifierBuffer = encoder.encodeQualifier(timestampDelta);
            ByteBuffer valueBuffer = encoder.encodeValue(expectedAgentStats);

            Buffer encodedQualifierBuffer = new FixedBuffer(qualifierBuffer.array());
            Buffer encodedValueBuffer = new FixedBuffer(valueBuffer.array());

            AgentStatDecodingContext context = new AgentStatDecodingContext();
            context.setAgentId(AGENT_ID);
            context.setBaseTimestamp(baseTimestamp);
            List<T> decodedAgentStats = decode(encodedQualifierBuffer, encodedValueBuffer, context);
            verify(expectedAgentStats, decodedAgentStats);
        }
    }

    protected void verify(List<T> expectedAgentStats, List<T> actualAgentStats) {
        Assert.assertEquals(expectedAgentStats, actualAgentStats);
    }

    private List<T> decode(Buffer encodedQualifierBuffer, Buffer encodedValueBuffer, AgentStatDecodingContext decodingContext) {
        long timestampDelta = decoder.decodeQualifier(encodedQualifierBuffer);
        decodingContext.setTimestampDelta(timestampDelta);
        return decoder.decodeValue(encodedValueBuffer, decodingContext);
    }
}
