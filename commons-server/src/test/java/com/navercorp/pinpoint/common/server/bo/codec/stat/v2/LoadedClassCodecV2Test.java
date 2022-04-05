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
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class LoadedClassCodecV2Test extends AgentStatCodecTestBase<LoadedClassBo> {

    @Autowired
    private AgentStatCodecV2<LoadedClassBo> codec;

    @Override
    protected List<LoadedClassBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createLoadedClassBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<LoadedClassBo> getCodec() {
        return codec;
    }

    @Override
    protected void verify(LoadedClassBo expected, LoadedClassBo actual) {
        Assert.assertEquals("agentId", expected.getAgentId(), actual.getAgentId());
        Assert.assertEquals("startTimestamp", expected.getStartTimestamp(), actual.getStartTimestamp());
        Assert.assertEquals("timestamp", expected.getTimestamp(), actual.getTimestamp());
        Assert.assertEquals("loadedClassCount", expected.getLoadedClassCount(), actual.getLoadedClassCount());
        Assert.assertEquals("unloadedClassCount", expected.getUnloadedClassCount(), actual.getUnloadedClassCount());
    }
}
