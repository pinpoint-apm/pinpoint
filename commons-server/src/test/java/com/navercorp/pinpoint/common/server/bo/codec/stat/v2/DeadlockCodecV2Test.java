/*
 * Copyright 2017 NAVER Corp.
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
import com.navercorp.pinpoint.common.server.bo.codec.stat.CodecTestConfig;
import com.navercorp.pinpoint.common.server.bo.codec.stat.TestAgentStatFactory;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

/**
 * @author Taejin Koo
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CodecTestConfig.class)
public class DeadlockCodecV2Test extends AgentStatCodecTestBase<DeadlockThreadCountBo> {

    @Autowired
    private AgentStatCodecV2<DeadlockThreadCountBo> codec;

    @Override
    protected List<DeadlockThreadCountBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createDeadlockBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<DeadlockThreadCountBo> getCodec() {
        return codec;
    }

    @Override
    protected void verify(DeadlockThreadCountBo expected, DeadlockThreadCountBo actual) {
        Assertions.assertEquals(expected.getAgentId(), actual.getAgentId(), "agentId");
        Assertions.assertEquals(expected.getStartTimestamp(), actual.getStartTimestamp(), "startTimestamp");
        Assertions.assertEquals(expected.getTimestamp(), actual.getTimestamp(), "timestamp");
        Assertions.assertEquals(expected.getDeadlockedThreadCount(), actual.getDeadlockedThreadCount(), "deadlockedThreadCount");
    }

}
