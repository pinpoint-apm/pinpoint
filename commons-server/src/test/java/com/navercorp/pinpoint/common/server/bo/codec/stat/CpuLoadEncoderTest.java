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

import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class CpuLoadEncoderTest extends AgentStatEncoderTestBase<CpuLoadBo> {

    private static final double DOUBLE_COMPARISON_DELTA = (double) 1 / AgentStatUtils.CONVERT_VALUE;

    @Override
    protected void verify(List<CpuLoadBo> expectedCpuLoadBos, List<CpuLoadBo> actualCpuLoadBos) {
        Assert.assertEquals(expectedCpuLoadBos.size(), actualCpuLoadBos.size());
        for (int i = 0; i < expectedCpuLoadBos.size(); ++i) {
            CpuLoadBo expectedCpuLoadBo = expectedCpuLoadBos.get(i);
            CpuLoadBo actualCpuLoadBo = actualCpuLoadBos.get(i);
            Assert.assertEquals("agentId", expectedCpuLoadBo.getAgentId(), actualCpuLoadBo.getAgentId());
            Assert.assertEquals("timestamp", expectedCpuLoadBo.getTimestamp(), actualCpuLoadBo.getTimestamp());
            Assert.assertEquals("agentStatType", expectedCpuLoadBo.getAgentStatType(), actualCpuLoadBo.getAgentStatType());
            Assert.assertEquals("jvmCpuLoad", expectedCpuLoadBo.getJvmCpuLoad(), actualCpuLoadBo.getJvmCpuLoad(), DOUBLE_COMPARISON_DELTA);
            Assert.assertEquals("systemCpuLoad", expectedCpuLoadBo.getSystemCpuLoad(), actualCpuLoadBo.getSystemCpuLoad(), DOUBLE_COMPARISON_DELTA);
        }
    }

    @Override
    protected List<CpuLoadBo> createAgentStats(String agentId, long initialTimestamp, int numStats) {
        return TestAgentStatFactory.createCpuLoadBos(agentId, initialTimestamp, numStats);
    }
}
