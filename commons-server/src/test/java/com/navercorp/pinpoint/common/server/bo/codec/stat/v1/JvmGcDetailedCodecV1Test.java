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
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class JvmGcDetailedCodecV1Test extends AgentStatCodecTestBase<JvmGcDetailedBo> {

    private static final double DOUBLE_COMPARISON_DELTA = (double) 1 / AgentStatUtils.CONVERT_VALUE;

    @Autowired
    private JvmGcDetailedCodecV1 jvmGcDetailedCodecV1;

    @Override
    protected List<JvmGcDetailedBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createJvmGcDetailedBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<JvmGcDetailedBo> getCodec() {
        return jvmGcDetailedCodecV1;
    }

    @Override
    protected void verify(JvmGcDetailedBo expectedJvmGcDetailedBo, JvmGcDetailedBo actualJvmGcDetailedBo) {
        Assert.assertEquals("agentId", expectedJvmGcDetailedBo.getAgentId(), actualJvmGcDetailedBo.getAgentId());
        Assert.assertEquals("timestamp", expectedJvmGcDetailedBo.getTimestamp(), actualJvmGcDetailedBo.getTimestamp());
        Assert.assertEquals("agentStatType", expectedJvmGcDetailedBo.getAgentStatType(), actualJvmGcDetailedBo.getAgentStatType());
        Assert.assertEquals("gcNewCount", expectedJvmGcDetailedBo.getGcNewCount(), actualJvmGcDetailedBo.getGcNewCount());
        Assert.assertEquals("gcNewTime", expectedJvmGcDetailedBo.getGcNewTime(), actualJvmGcDetailedBo.getGcNewTime());
        Assert.assertEquals("codeCacheUsed", expectedJvmGcDetailedBo.getCodeCacheUsed(), actualJvmGcDetailedBo.getCodeCacheUsed(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals("codeCacheUsed", expectedJvmGcDetailedBo.getCodeCacheUsed(), actualJvmGcDetailedBo.getCodeCacheUsed(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals("newGenUsed", expectedJvmGcDetailedBo.getNewGenUsed(), actualJvmGcDetailedBo.getNewGenUsed(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals("oldGenUsed", expectedJvmGcDetailedBo.getOldGenUsed(), actualJvmGcDetailedBo.getOldGenUsed(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals("survivorSpaceUsed", expectedJvmGcDetailedBo.getSurvivorSpaceUsed(), actualJvmGcDetailedBo.getSurvivorSpaceUsed(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals("permGenUsed", expectedJvmGcDetailedBo.getPermGenUsed(), actualJvmGcDetailedBo.getPermGenUsed(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals("metaspaceUsed", expectedJvmGcDetailedBo.getMetaspaceUsed(), actualJvmGcDetailedBo.getMetaspaceUsed(), DOUBLE_COMPARISON_DELTA);
    }
}
