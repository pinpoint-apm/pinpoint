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
import com.navercorp.pinpoint.common.server.bo.codec.stat.CodecTestConfig;
import com.navercorp.pinpoint.common.server.bo.codec.stat.TestAgentStatFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
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
@ContextConfiguration(classes = CodecTestConfig.class)
public class JvmGcDetailedCodecV2Test extends AgentStatCodecTestBase<JvmGcDetailedBo> {

    private static final double DOUBLE_COMPARISON_DELTA = (double) 1 / AgentStatUtils.CONVERT_VALUE;

    @Autowired
    private AgentStatCodecV2<JvmGcDetailedBo> codec;

    @Override
    protected List<JvmGcDetailedBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createJvmGcDetailedBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<JvmGcDetailedBo> getCodec() {
        return codec;
    }

    @Override
    protected void verify(JvmGcDetailedBo expected, JvmGcDetailedBo actual) {
        Assertions.assertEquals(expected.getAgentId(), actual.getAgentId(), "agentId");
        Assertions.assertEquals(expected.getStartTimestamp(), actual.getStartTimestamp(), "startTimestamp");
        Assertions.assertEquals(expected.getTimestamp(), actual.getTimestamp(), "timestamp");
        Assertions.assertEquals(expected.getAgentStatType(), actual.getAgentStatType(), "agentStatType");
        Assertions.assertEquals(expected.getGcNewCount(), actual.getGcNewCount(), "gcNewCount");
        Assertions.assertEquals(expected.getGcNewTime(), actual.getGcNewTime(), "gcNewTime");
        Assertions.assertEquals(expected.getCodeCacheUsed(), actual.getCodeCacheUsed(), DOUBLE_COMPARISON_DELTA, "codeCacheUsed");
        Assertions.assertEquals(expected.getCodeCacheUsed(), actual.getCodeCacheUsed(), DOUBLE_COMPARISON_DELTA, "codeCacheUsed");
        Assertions.assertEquals(expected.getNewGenUsed(), actual.getNewGenUsed(), DOUBLE_COMPARISON_DELTA, "newGenUsed");
        Assertions.assertEquals(expected.getOldGenUsed(), actual.getOldGenUsed(), DOUBLE_COMPARISON_DELTA, "oldGenUsed");
        Assertions.assertEquals(expected.getSurvivorSpaceUsed(), actual.getSurvivorSpaceUsed(), DOUBLE_COMPARISON_DELTA, "survivorSpaceUsed");
        Assertions.assertEquals(expected.getPermGenUsed(), actual.getPermGenUsed(), DOUBLE_COMPARISON_DELTA, "permGenUsed");
        Assertions.assertEquals(expected.getMetaspaceUsed(), actual.getMetaspaceUsed(), DOUBLE_COMPARISON_DELTA, "metaspaceUsed");
    }
}
