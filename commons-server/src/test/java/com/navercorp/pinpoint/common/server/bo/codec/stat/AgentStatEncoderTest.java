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

import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class AgentStatEncoderTest {

    @Autowired
    private TestAgentStatEncoder encoder;

    @Autowired
    private TestAgentStatDecoder decoder;

    @Test
    public void test_encodeQualifier() {
        // Given
        final long expectedInitialTimestamp = System.currentTimeMillis();
        final long baseTimestamp = AgentStatUtils.getBaseTimestamp(expectedInitialTimestamp);
        final List<AgentStatDataPoint> givenDataPoint = createAgentStatDataPoints(expectedInitialTimestamp);
        // When
        ByteBuffer qualifierBuffer = this.encoder.encodeQualifier(givenDataPoint);
        // Then
        long actualInitialTimestamp = this.decoder.decodeInitialTimestamp(baseTimestamp, new FixedBuffer(qualifierBuffer.array()));
        Assert.assertEquals(expectedInitialTimestamp, actualInitialTimestamp);
    }

    private List<AgentStatDataPoint> createAgentStatDataPoints(long... timestamps) {
        List<AgentStatDataPoint> dataPoints = new ArrayList<>(timestamps.length);
        for (long timestamp : timestamps) {
            dataPoints.add(new TestAgentStatDataPoint("testAgentId", timestamp));
        }
        return dataPoints;
    }

    @Component
    private static class TestAgentStatEncoder extends AgentStatEncoder<AgentStatDataPoint> {
        private TestAgentStatEncoder() {
            super(null);
        }
    }

    @Component
    private static class TestAgentStatDecoder extends AgentStatDecoder<AgentStatDataPoint> {
        private TestAgentStatDecoder() {
            super(Collections.<AgentStatCodec<AgentStatDataPoint>>emptyList());
        }
    }

    private static class TestAgentStatDataPoint implements AgentStatDataPoint {

        private String agentId;
        private long timestamp;

        private TestAgentStatDataPoint(String agentId, long timestamp) {
            this.agentId = agentId;
            this.timestamp = timestamp;
        }

        @Override
        public String getAgentId() {
            return agentId;
        }

        @Override
        public void setAgentId(String agentId) {
            this.agentId = agentId;
        }

        @Override
        public long getTimestamp() {
            return this.timestamp;
        }

        @Override
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public AgentStatType getAgentStatType() {
            return AgentStatType.UNKNOWN;
        }
    }
}
