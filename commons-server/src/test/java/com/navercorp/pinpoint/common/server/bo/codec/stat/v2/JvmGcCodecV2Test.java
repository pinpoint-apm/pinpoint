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
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class JvmGcCodecV2Test extends AgentStatCodecTestBase<JvmGcBo> {

    @Autowired
    @Qualifier("jvmGcCodecV2")
    private AgentStatCodec<JvmGcBo> codec;

    @Override
    protected List<JvmGcBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createJvmGcBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<JvmGcBo> getCodec() {
        return codec;
    }

    @Override
    protected void verify(JvmGcBo expected, JvmGcBo actual) {
        Assertions.assertEquals(expected.getAgentId(), actual.getAgentId(), "agentId");
        Assertions.assertEquals(expected.getStartTimestamp(), actual.getStartTimestamp(), "startTimestamp");
        Assertions.assertEquals(expected.getTimestamp(), actual.getTimestamp(), "timestamp");
        Assertions.assertEquals(expected.getGcType(), actual.getGcType(), "gcType");
        Assertions.assertEquals(expected.getHeapUsed(), actual.getHeapUsed(), "heapUsed");
        Assertions.assertEquals(expected.getHeapMax(), actual.getHeapMax(), "heapMax");
        Assertions.assertEquals(expected.getNonHeapUsed(), actual.getNonHeapUsed(), "nonHeapUsed");
        Assertions.assertEquals(expected.getNonHeapMax(), actual.getNonHeapMax(), "nonHeapMax");
        Assertions.assertEquals(expected.getGcOldCount(), actual.getGcOldCount(), "gcOldCount");
        Assertions.assertEquals(expected.getGcOldTime(), actual.getGcOldTime(), "gcOldTime");
    }
}
