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
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
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
public class TransactionCodecV1Test extends AgentStatCodecTestBase<TransactionBo> {

    @Autowired
    private TransactionCodecV1 transactionCodecV1;

    @Override
    protected List<TransactionBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createTransactionBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<TransactionBo> getCodec() {
        return transactionCodecV1;
    }

    @Override
    protected void verify(TransactionBo expected, TransactionBo actual) {
        Assert.assertEquals("agentId", expected.getAgentId(), actual.getAgentId());
        Assert.assertEquals("timestamp", expected.getTimestamp(), actual.getTimestamp());
        Assert.assertEquals("collectInterval", expected.getCollectInterval(), actual.getCollectInterval());
        Assert.assertEquals("sampledNewCount", expected.getSampledNewCount(), actual.getSampledNewCount());
        Assert.assertEquals("sampledContinuationCount", expected.getSampledContinuationCount(), actual.getSampledContinuationCount());
        Assert.assertEquals("unsampledNewCount", expected.getUnsampledNewCount(), actual.getUnsampledNewCount());
        Assert.assertEquals("unsampledContinuationCount", expected.getUnsampledContinuationCount(), actual.getUnsampledContinuationCount());
    }
}
