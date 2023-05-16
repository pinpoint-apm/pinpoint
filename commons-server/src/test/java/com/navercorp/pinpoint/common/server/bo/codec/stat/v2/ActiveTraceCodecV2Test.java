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

package com.navercorp.pinpoint.common.server.bo.codec.stat.v2;

import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodecTestBase;
import com.navercorp.pinpoint.common.server.bo.codec.stat.TestAgentStatFactory;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ActiveTraceCodecV2Test extends AgentStatCodecTestBase<ActiveTraceBo> {

    @Autowired
    private AgentStatCodecV2<ActiveTraceBo> codec;

    @Override
    protected List<ActiveTraceBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createActiveTraceBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<ActiveTraceBo> getCodec() {
        return codec;
    }

    @Override
    protected void verify(ActiveTraceBo expected, ActiveTraceBo actual) {
        Assertions.assertEquals(expected.getAgentId(), actual.getAgentId(), "agentId");
        Assertions.assertEquals(expected.getStartTimestamp(), actual.getStartTimestamp(), "startTimestamp");
        Assertions.assertEquals(expected.getTimestamp(), actual.getTimestamp(), "timestamp");
        Assertions.assertEquals(expected.getVersion(), actual.getVersion(), "version");
        Assertions.assertEquals(expected.getHistogramSchemaType(), actual.getHistogramSchemaType(), "histogramSchemaType");
        Assertions.assertEquals(expected.getActiveTraceHistogram(), actual.getActiveTraceHistogram(), "activeTraceCounts");
    }
}
