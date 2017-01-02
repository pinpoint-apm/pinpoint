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
public class JvmGcDetailedCodecV2Test extends AgentStatCodecTestBase<JvmGcDetailedBo> {

    private static final double DOUBLE_COMPARISON_DELTA = (double) 1 / AgentStatUtils.CONVERT_VALUE;

    @Autowired
    private JvmGcDetailedCodecV2 jvmGcDetailedCodecV2;

    @Override
    protected List<JvmGcDetailedBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createJvmGcDetailedBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<JvmGcDetailedBo> getCodec() {
        return jvmGcDetailedCodecV2;
    }

    @Override
    protected void verify(JvmGcDetailedBo expected, JvmGcDetailedBo actual) {
        Assert.assertEquals("agentId", expected.getAgentId(), actual.getAgentId());
        Assert.assertEquals("startTimestamp", expected.getStartTimestamp(), actual.getStartTimestamp());
        Assert.assertEquals("timestamp", expected.getTimestamp(), actual.getTimestamp());
        Assert.assertEquals("agentStatType", expected.getAgentStatType(), actual.getAgentStatType());
        Assert.assertEquals("gcNewCount", expected.getGcNewCount(), actual.getGcNewCount());
        Assert.assertEquals("gcNewTime", expected.getGcNewTime(), actual.getGcNewTime());
        Assert.assertEquals("codeCacheUsed", expected.getCodeCacheUsed(), actual.getCodeCacheUsed(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals("codeCacheUsed", expected.getCodeCacheUsed(), actual.getCodeCacheUsed(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals("newGenUsed", expected.getNewGenUsed(), actual.getNewGenUsed(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals("oldGenUsed", expected.getOldGenUsed(), actual.getOldGenUsed(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals("survivorSpaceUsed", expected.getSurvivorSpaceUsed(), actual.getSurvivorSpaceUsed(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals("permGenUsed", expected.getPermGenUsed(), actual.getPermGenUsed(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals("metaspaceUsed", expected.getMetaspaceUsed(), actual.getMetaspaceUsed(), DOUBLE_COMPARISON_DELTA);
    }
}
