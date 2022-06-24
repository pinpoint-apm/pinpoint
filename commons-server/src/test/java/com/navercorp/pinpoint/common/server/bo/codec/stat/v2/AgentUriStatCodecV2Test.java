/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.EachUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.UriStatHistogram;
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
public class AgentUriStatCodecV2Test extends AgentStatCodecTestBase<AgentUriStatBo> {

    @Autowired
    private AgentStatCodec<AgentUriStatBo> codec;

    @Override
    protected List<AgentUriStatBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createAgentUriStatBo(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<AgentUriStatBo> getCodec() {
        return codec;
    }

    @Override
    protected void verify(AgentUriStatBo expected, AgentUriStatBo actual) {
        Assertions.assertEquals(expected.getAgentId(), actual.getAgentId());
        Assertions.assertEquals(expected.getStartTimestamp(), actual.getStartTimestamp());
        Assertions.assertEquals(expected.getTimestamp(), actual.getTimestamp());
        Assertions.assertEquals(expected.getAgentStatType(), actual.getAgentStatType());
        Assertions.assertEquals(expected.getBucketVersion(), actual.getBucketVersion());

        List<EachUriStatBo> expectedEachUriStatBoList = expected.getEachUriStatBoList();
        List<EachUriStatBo> actualEachUriStatBoList = actual.getEachUriStatBoList();

        Assertions.assertEquals(expectedEachUriStatBoList.size(), actualEachUriStatBoList.size());

        int eachUriStatBoSize = actualEachUriStatBoList.size();
        for (int i = 0; i < eachUriStatBoSize; i++) {
            assertEachUriStatBo(expectedEachUriStatBoList.get(i), actualEachUriStatBoList.get(i));
        }
    }

    private void assertEachUriStatBo(EachUriStatBo expected, EachUriStatBo actual) {
        Assertions.assertEquals(expected.getUri(), actual.getUri());

        assertUriStatHistogram(expected.getTotalHistogram(), actual.getTotalHistogram());
        assertUriStatHistogram(expected.getFailedHistogram(), actual.getFailedHistogram());
    }

    private void assertUriStatHistogram(UriStatHistogram expected, UriStatHistogram actual) {
        if (expected == null && actual == null) {
            return;
        }
        Assertions.assertEquals(expected.getCount(), actual.getCount());
        Assertions.assertEquals(AgentStatUtils.convertDoubleToLong(expected.getAvg()), AgentStatUtils.convertDoubleToLong(actual.getAvg()));
        Assertions.assertEquals(expected.getMax(), actual.getMax());
        Assertions.assertArrayEquals(expected.getTimestampHistogram(), actual.getTimestampHistogram());
    }

}

