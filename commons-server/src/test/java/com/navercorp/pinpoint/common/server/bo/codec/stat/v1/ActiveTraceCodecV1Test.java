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

package com.navercorp.pinpoint.common.server.bo.codec.stat.v1;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.TestAgentStatFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.trace.SlotType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ActiveTraceCodecV1Test {

    private static final String AGENT_ID = "testAgentId";
    private static final int NUM_TEST_RUNS = 20;

    @Autowired
    private ActiveTraceCodecV1 activeTraceCodec;

    @Test
    public void should_be_encoded_and_decoded_to_same_value() {
        for (int i = 0; i < NUM_TEST_RUNS; ++i) {
            runTest();
        }
    }

    private void runTest() {
        // Given
        final long initialTimestamp = System.currentTimeMillis();
        final long baseTimestamp = AgentStatUtils.getBaseTimestamp(initialTimestamp);
        final long timestampDelta = initialTimestamp - baseTimestamp;
        final List<ActiveTraceBo> expectedActiveTraceBos = TestAgentStatFactory.createActiveTraceBos(AGENT_ID, initialTimestamp);
        // When
        Buffer encodedValueBuffer = new AutomaticBuffer();
        this.activeTraceCodec.encodeValues(encodedValueBuffer, expectedActiveTraceBos);
        // Then
        AgentStatDecodingContext decodingContext = new AgentStatDecodingContext();
        decodingContext.setAgentId(AGENT_ID);
        decodingContext.setBaseTimestamp(baseTimestamp);
        decodingContext.setTimestampDelta(timestampDelta);
        Buffer valueBuffer = new FixedBuffer(encodedValueBuffer.getBuffer());
        List<ActiveTraceBo> actualActiveTraceBos = this.activeTraceCodec.decodeValues(valueBuffer, decodingContext);
        Assert.assertEquals(expectedActiveTraceBos, actualActiveTraceBos);
    }

    @Test
    public void empty_active_trace_counts_should_be_encoded_and_decoded() {
        // Given
        final int numValues = 20;
        final long initialTimestamp = System.currentTimeMillis();
        final long baseTimestamp = AgentStatUtils.getBaseTimestamp(initialTimestamp);
        final long timestampDelta = initialTimestamp - baseTimestamp;
        final List<ActiveTraceBo> expectedActiveTraceBos = new ArrayList<>(numValues);
        for (int i = 0; i < numValues; ++i) {
            ActiveTraceBo emptyActiveTraceBo = new ActiveTraceBo();
            emptyActiveTraceBo.setAgentId(AGENT_ID);
            emptyActiveTraceBo.setTimestamp(initialTimestamp + i);
            emptyActiveTraceBo.setActiveTraceCounts(Collections.<SlotType, Integer>emptyMap());
            expectedActiveTraceBos.add(emptyActiveTraceBo);
        }
        // When
        Buffer encodedValueBuffer = new AutomaticBuffer();
        this.activeTraceCodec.encodeValues(encodedValueBuffer, expectedActiveTraceBos);
        // Then
        AgentStatDecodingContext decodingContext = new AgentStatDecodingContext();
        decodingContext.setAgentId(AGENT_ID);
        decodingContext.setBaseTimestamp(baseTimestamp);
        decodingContext.setTimestampDelta(timestampDelta);
        Buffer valueBuffer = new FixedBuffer(encodedValueBuffer.getBuffer());
        List<ActiveTraceBo> actualActiveTraceBos = this.activeTraceCodec.decodeValues(valueBuffer, decodingContext);
        for (ActiveTraceBo actualActiveTraceBo : actualActiveTraceBos) {
            Map<SlotType, Integer> activeTraceCounts = actualActiveTraceBo.getActiveTraceCounts();
            for (int activeTraceCount : activeTraceCounts.values()) {
                Assert.assertEquals(ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT, activeTraceCount);
            }
        }
    }
}
