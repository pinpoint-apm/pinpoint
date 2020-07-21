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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.IntCounter;
import com.navercorp.pinpoint.bootstrap.util.jdk.ThreadLocalRandom;
import com.navercorp.pinpoint.common.server.bo.metric.AgentCustomMetricBo;
import com.navercorp.pinpoint.common.server.bo.metric.CustomMetricType;
import com.navercorp.pinpoint.common.server.bo.metric.DefaultCustomMetricType;
import com.navercorp.pinpoint.common.server.bo.metric.EachCustomMetricBo;
import com.navercorp.pinpoint.common.server.bo.metric.FieldDescriptor;
import com.navercorp.pinpoint.common.server.bo.metric.FieldDescriptors;
import com.navercorp.pinpoint.common.server.bo.metric.IntCounterMetricValue;
import com.navercorp.pinpoint.common.server.bo.metric.IntCounterMetricValueList;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class AgentCustomMetricDispatchServiceTest {

    @InjectMocks
    private AgentCustomMetricDispatchService agentCustomMetricDispatchService;

    @Mock
    private AgentCustomMetricServiceLocator agentCustomMetricServiceLocator;

    @Test
    public void dispatchTest1() {
        final AgentCustomMetricBo agentCustomMetricBo = new AgentCustomMetricBo();
        agentCustomMetricBo.setAgentId("agentId");
        agentCustomMetricBo.setStartTimestamp(System.currentTimeMillis());


        final String metricName1 = "testMetric1";
        final IntCounterMetricValueList testMetric1 = createRandomIntCounterMetricValueList(metricName1, 100, 7);
        agentCustomMetricBo.addIntCounterMetricValueList(testMetric1);

        final String includedMetricName = "testMetric2";
        final IntCounterMetricValueList testMetric2 = createRandomIntCounterMetricValueList(includedMetricName, 100, 7);
        agentCustomMetricBo.addIntCounterMetricValueList(testMetric2);

        final MockAgentCustomMetricService mockAgentCustomMetricService1 = createMockAgentCustomMetricService(includedMetricName);
        final MockAgentCustomMetricService mockAgentCustomMetricService2 = createMockAgentCustomMetricService("testMetric4");

        Mockito.when(agentCustomMetricServiceLocator.getAgentCustomMetricService()).thenReturn(Arrays.asList(mockAgentCustomMetricService1, mockAgentCustomMetricService2));

        agentCustomMetricDispatchService.save(agentCustomMetricBo);

        List<EachCustomMetricBo> eachCustomMetricBoList = mockAgentCustomMetricService1.getEachCustomMetricBoList();
        for (EachCustomMetricBo eachCustomMetricBo : eachCustomMetricBoList) {
            final Set<String> keys = eachCustomMetricBo.keySet();
            Assert.assertTrue(keys.contains(includedMetricName));
            Assert.assertFalse(keys.contains(metricName1));
        }

        eachCustomMetricBoList = mockAgentCustomMetricService2.getEachCustomMetricBoList();
        Assert.assertNull(eachCustomMetricBoList);
    }

    private MockAgentCustomMetricService createMockAgentCustomMetricService(String metricName) {
        FieldDescriptors.Builder builder = new FieldDescriptors.Builder();
        builder.add(new FieldDescriptor(0, metricName, IntCounter.class));

        return new MockAgentCustomMetricService(new DefaultCustomMetricType(AgentStatType.NETTY_DIRECT_BUFFER, builder.build()));
    }

    private IntCounterMetricValueList createRandomIntCounterMetricValueList(String metricName, int bound, int size) {
        final IntCounterMetricValueList intCounterMetricValueList = new IntCounterMetricValueList(metricName);

        for (int i = 0; i < size; i++) {
            intCounterMetricValueList.add(createRandomIntCounterMetricValue(metricName, bound));
        }

        return intCounterMetricValueList;
    }


    private IntCounterMetricValue createRandomIntCounterMetricValue(String metricName, int bound) {
        final int value = ThreadLocalRandom.current().nextInt(0, bound);

        final IntCounterMetricValue customMetricValue = new IntCounterMetricValue();
        customMetricValue.setMetricName(metricName);
        customMetricValue.setValue(value);

        return customMetricValue;
    }

    private static class MockAgentCustomMetricService extends AbstractAgentCustomMetricService {

        private List<EachCustomMetricBo> eachCustomMetricBoList;

        public MockAgentCustomMetricService(CustomMetricType customMetricType) {
            super(customMetricType);
        }

        @Override
        public void save(String agentId, List<EachCustomMetricBo> eachCustomMetricBoList) {
            this.eachCustomMetricBoList = eachCustomMetricBoList;
        }

        public List<EachCustomMetricBo> getEachCustomMetricBoList() {
            return eachCustomMetricBoList;
        }

    }

}
