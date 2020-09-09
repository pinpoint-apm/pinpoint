/*
 * Copyright 2020 NAVER Corp.
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
import com.navercorp.pinpoint.common.server.bo.stat.ContainerBo;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author Hyunjoon Cho
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ContainerCodecV2Test extends AgentStatCodecTestBase<ContainerBo> {
    private static final double DOUBLE_COMPARISON_DELTA = (double) 1 / AgentStatUtils.CONVERT_VALUE;

    @Autowired
    private ContainerCodecV2 containerCodecV2;

    @Override
    protected List<ContainerBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createContainerBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<ContainerBo> getCodec() {
        return containerCodecV2;
    }

    @Override
    protected void verify(ContainerBo expected, ContainerBo actual) {
        Assert.assertEquals("agentId", expected.getAgentId(), actual.getAgentId());
        Assert.assertEquals("startTimestamp", expected.getStartTimestamp(), actual.getStartTimestamp());
        Assert.assertEquals("timestamp", expected.getTimestamp(), actual.getTimestamp());
        Assert.assertEquals("agentStatType", expected.getAgentStatType(), actual.getAgentStatType());
        Assert.assertEquals("userCpuUsage", expected.getUserCpuUsage(), actual.getUserCpuUsage(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals("systemCpuUsage", expected.getSystemCpuUsage(), actual.getSystemCpuUsage(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals("memoryMax", expected.getMemoryMax(), actual.getMemoryMax());
        Assert.assertEquals("memoryUsage", expected.getMemoryUsage(), actual.getMemoryUsage());
    }
}
