/*
 * Copyright 2018 Naver Corp.
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
import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author Roy Kim
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class DirectBufferCodecV2Test extends AgentStatCodecTestBase<DirectBufferBo> {

    @Autowired
    private DirectBufferCodecV2 directBufferCodecV2;

    @Override
    protected List<DirectBufferBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createDirectBufferBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<DirectBufferBo> getCodec() {
        return directBufferCodecV2;
    }

    @Override
    protected void verify(DirectBufferBo expected, DirectBufferBo actual) {
        Assert.assertEquals("agentId", expected.getAgentId(), actual.getAgentId());
        Assert.assertEquals("startTimestamp", expected.getStartTimestamp(), actual.getStartTimestamp());
        Assert.assertEquals("timestamp", expected.getTimestamp(), actual.getTimestamp());
        Assert.assertEquals("agentStatType", expected.getAgentStatType(), actual.getAgentStatType());
        Assert.assertEquals("directCount", expected.getDirectCount(), actual.getDirectCount());
        Assert.assertEquals("directMemoryUsed", expected.getDirectMemoryUsed(), actual.getDirectMemoryUsed());
        Assert.assertEquals("mappedCount", expected.getMappedCount(), actual.getMappedCount());
        Assert.assertEquals("mappedMemoryUsed", expected.getMappedMemoryUsed(), actual.getMappedMemoryUsed());
    }
}
