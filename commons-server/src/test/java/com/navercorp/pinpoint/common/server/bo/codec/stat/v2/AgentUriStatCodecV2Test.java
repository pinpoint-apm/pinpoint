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

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

/**
 * @author Taejin Koo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class AgentUriStatCodecV2Test extends AgentStatCodecTestBase<AgentUriStatBo> {

    @Autowired
    private AgentUriStatCodecV2 agentUriStatCodecV2;

    @Override
    protected List<AgentUriStatBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        List<AgentUriStatBo> agentUriStatBo = TestAgentStatFactory.createAgentUriStatBo(agentId, startTimestamp, initialTimestamp);
        return agentUriStatBo;
    }

    @Override
    protected AgentStatCodec<AgentUriStatBo> getCodec() {
        return agentUriStatCodecV2;
    }

    @Override
    protected void verify(AgentUriStatBo expected, AgentUriStatBo actual) {
        Assert.assertEquals(expected.getAgentId(), actual.getAgentId());
        Assert.assertEquals(expected.getStartTimestamp(), actual.getStartTimestamp());
        Assert.assertEquals(expected.getTimestamp(), actual.getTimestamp());
        Assert.assertEquals(expected.getAgentStatType(), actual.getAgentStatType());
        Assert.assertEquals(expected.getBucketVersion(), actual.getBucketVersion());

        List<EachUriStatBo> expectedEachUriStatBoList = expected.getEachUriStatBoList();
        List<EachUriStatBo> actualEachUriStatBoList = actual.getEachUriStatBoList();

        Assert.assertEquals(expectedEachUriStatBoList.size(), actualEachUriStatBoList.size());

        int eachUriStatBoSize = actualEachUriStatBoList.size();
        for (int i = 0; i < eachUriStatBoSize; i++) {
            assertEachUriStatBo(expectedEachUriStatBoList.get(i), actualEachUriStatBoList.get(i));
        }
    }

    private void assertEachUriStatBo(EachUriStatBo expected, EachUriStatBo actual) {
        Assert.assertEquals(expected.getUri(), actual.getUri());

        assertUriStatHistogram(expected.getTotalHistogram(), actual.getTotalHistogram());
        assertUriStatHistogram(expected.getFailedHistogram(), actual.getFailedHistogram());
    }

    private void assertUriStatHistogram(UriStatHistogram expected, UriStatHistogram actual) {
        if (expected == null && actual == null) {
            return;
        }
        Assert.assertEquals(expected.getCount(), actual.getCount());
        Assert.assertEquals(AgentStatUtils.convertDoubleToLong(expected.getAvg()), AgentStatUtils.convertDoubleToLong(actual.getAvg()));
        Assert.assertEquals(expected.getMax(), actual.getMax());
        Assert.assertTrue(Arrays.equals(expected.getTimestampHistogram(), actual.getTimestampHistogram()));
    }

}

