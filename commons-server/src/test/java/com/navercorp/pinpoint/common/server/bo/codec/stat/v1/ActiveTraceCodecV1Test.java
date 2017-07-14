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

import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodecTestBase;
import com.navercorp.pinpoint.common.server.bo.codec.stat.TestAgentStatFactory;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.trace.SlotType;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ActiveTraceCodecV1Test extends AgentStatCodecTestBase<ActiveTraceBo> {

    @Autowired
    private ActiveTraceCodecV1 activeTraceCodecV1;

    @Override
    protected List<ActiveTraceBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createActiveTraceBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<ActiveTraceBo> getCodec() {
        return activeTraceCodecV1;
    }

    @Override
    protected void verify(ActiveTraceBo expected, ActiveTraceBo actual) {
        Assert.assertEquals("agentId", expected.getAgentId(), actual.getAgentId());
        Assert.assertEquals("timestamp", expected.getTimestamp(), actual.getTimestamp());
        Assert.assertEquals("version", expected.getVersion(), actual.getVersion());
        Assert.assertEquals("histogramSchemaType", expected.getHistogramSchemaType(), actual.getHistogramSchemaType());
        if (CollectionUtils.isEmpty(expected.getActiveTraceCounts())) {
            for (Map.Entry<SlotType, Integer> e : actual.getActiveTraceCounts().entrySet()) {
                SlotType slotType = e.getKey();
                int activeTraceCount = e.getValue();
                Assert.assertEquals("activeTraceCount [" + slotType + "]", ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT, activeTraceCount);
            }
        } else {
            Assert.assertEquals("activeTraceCounts", expected.getActiveTraceCounts(), actual.getActiveTraceCounts());
        }
    }
}
