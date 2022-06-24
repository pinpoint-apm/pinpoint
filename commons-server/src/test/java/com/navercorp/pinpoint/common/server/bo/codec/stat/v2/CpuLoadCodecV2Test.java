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
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
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
public class CpuLoadCodecV2Test extends AgentStatCodecTestBase<CpuLoadBo> {

    private static final double DOUBLE_COMPARISON_DELTA = (double) 1 / AgentStatUtils.CONVERT_VALUE;

    @Autowired
    private AgentStatCodecV2<CpuLoadBo> codec;

    @Override
    protected List<CpuLoadBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createCpuLoadBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<CpuLoadBo> getCodec() {
        return codec;
    }

    @Override
    protected void verify(CpuLoadBo expected, CpuLoadBo actual) {
        Assertions.assertEquals(expected.getAgentId(), actual.getAgentId(), "agentId");
        Assertions.assertEquals(expected.getStartTimestamp(), actual.getStartTimestamp(), "startTimestamp");
        Assertions.assertEquals(expected.getTimestamp(), actual.getTimestamp(), "timestamp");
        Assertions.assertEquals(expected.getAgentStatType(), actual.getAgentStatType(), "agentStatType");
        Assertions.assertEquals(expected.getJvmCpuLoad(), actual.getJvmCpuLoad(), DOUBLE_COMPARISON_DELTA, "jvmCpuLoad");
        Assertions.assertEquals(expected.getSystemCpuLoad(), actual.getSystemCpuLoad(), DOUBLE_COMPARISON_DELTA, "systemCpuLoad");
    }
}
