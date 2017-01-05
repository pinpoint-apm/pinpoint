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
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
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
public class JvmGcCodecV1Test extends AgentStatCodecTestBase<JvmGcBo> {

    @Autowired
    private JvmGcCodecV1 jvmGcCodecV1;

    @Override
    protected List<JvmGcBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createJvmGcBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<JvmGcBo> getCodec() {
        return jvmGcCodecV1;
    }

    @Override
    protected void verify(JvmGcBo expected, JvmGcBo actual) {
        Assert.assertEquals("agentId", expected.getAgentId(), actual.getAgentId());
        Assert.assertEquals("timestamp", expected.getTimestamp(), actual.getTimestamp());
        Assert.assertEquals("gcType", expected.getGcType(), actual.getGcType());
        Assert.assertEquals("heapUsed", expected.getHeapUsed(), actual.getHeapUsed());
        Assert.assertEquals("heapMax", expected.getHeapMax(), actual.getHeapMax());
        Assert.assertEquals("nonHeapUsed", expected.getNonHeapUsed(), actual.getNonHeapUsed());
        Assert.assertEquals("nonHeapMax", expected.getNonHeapMax(), actual.getNonHeapMax());
        Assert.assertEquals("gcOldCount", expected.getGcOldCount(), actual.getGcOldCount());
        Assert.assertEquals("gcOldTime", expected.getGcOldTime(), actual.getGcOldTime());
    }
}
