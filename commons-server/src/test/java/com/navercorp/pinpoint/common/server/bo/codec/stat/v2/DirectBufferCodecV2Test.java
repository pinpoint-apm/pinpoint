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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

/**
 * @author Roy Kim
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class DirectBufferCodecV2Test extends AgentStatCodecTestBase<DirectBufferBo> {

    @Autowired
    private AgentStatCodecV2<DirectBufferBo> codec;

    @Override
    protected List<DirectBufferBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createDirectBufferBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<DirectBufferBo> getCodec() {
        return codec;
    }

    @Override
    protected void verify(DirectBufferBo expected, DirectBufferBo actual) {
        Assertions.assertEquals(expected.getAgentId(), actual.getAgentId(), "agentId");
        Assertions.assertEquals(expected.getStartTimestamp(), actual.getStartTimestamp(), "startTimestamp");
        Assertions.assertEquals(expected.getTimestamp(), actual.getTimestamp(), "timestamp");
        Assertions.assertEquals(expected.getAgentStatType(), actual.getAgentStatType(), "agentStatType");
        Assertions.assertEquals(expected.getDirectCount(), actual.getDirectCount(), "directCount");
        Assertions.assertEquals(expected.getDirectMemoryUsed(), actual.getDirectMemoryUsed(), "directMemoryUsed");
        Assertions.assertEquals(expected.getMappedCount(), actual.getMappedCount(), "mappedCount");
        Assertions.assertEquals(expected.getMappedMemoryUsed(), actual.getMappedMemoryUsed(), "mappedMemoryUsed");
    }
}
