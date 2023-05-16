/*
 * Copyright 2017 NAVER Corp.
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
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

/**
 * @author Taejin Koo
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ResponseTimeCodecV2Test extends AgentStatCodecTestBase<ResponseTimeBo> {

    @Autowired
    private AgentStatCodecV2<ResponseTimeBo> codec;

    @Override
    protected List<ResponseTimeBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createResponseTimeBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<ResponseTimeBo> getCodec() {
        return codec;
    }

    @Override
    protected void verify(ResponseTimeBo expected, ResponseTimeBo actual) {
        Assertions.assertEquals(expected.getAgentId(), actual.getAgentId(), "agentId");
        Assertions.assertEquals(expected.getStartTimestamp(), actual.getStartTimestamp(), "startTimestamp");
        Assertions.assertEquals(expected.getTimestamp(), actual.getTimestamp(), "timestamp");
        Assertions.assertEquals(expected.getAvg(), actual.getAvg(), "avg");
    }

}

