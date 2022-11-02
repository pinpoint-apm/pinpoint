/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.collector.dao.pinot;

import com.navercorp.pinpoint.metric.collector.view.SystemMetricView;
import com.navercorp.pinpoint.metric.common.model.DoubleMetric;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

/**
 * @author Hyunjoon Cho
 */
@ExtendWith(MockitoExtension.class)
public class PinotSystemMetricDaoTest {

    private final static String TOPIC = "test-topic";
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final Random random = new Random(System.currentTimeMillis());
    private final SendCount sendCount = new SendCount();

    @Mock
    private KafkaTemplate<String, SystemMetricView> kafkaTemplate;
    @Mock
    private DoubleMetric doubleMetric;

    @BeforeEach
    public void setupTemplate() {

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                sendCount.increase();
                logger.info("Sending View {}", sendCount.getSendCount());
                return null;
            }
        }).when(kafkaTemplate).send(anyString(), anyString(), any(SystemMetricView.class));
    }


    @Test
    public void testLogDao() {
        PinotSystemMetricDoubleDao longDao = new PinotSystemMetricDoubleDao(kafkaTemplate, TOPIC);
        List<DoubleMetric> doubleMetricList = createDoubleCounterList();

        longDao.insert("hostGroupName", "hostName", doubleMetricList);

        Assertions.assertEquals(doubleMetricList.size(), sendCount.getSendCount());
    }

    private List<DoubleMetric> createDoubleCounterList() {
        List<DoubleMetric> doubleMetricList = new ArrayList<>();
        int numCounter = random.nextInt(100);
        for (int i = 0; i < numCounter; i++) {
            doubleMetricList.add(doubleMetric);
        }

        return doubleMetricList;
    }

    public class SendCount {
        private int sendCount = 0;

        public void setZero() {
            sendCount = 0;
        }

        public void increase() {
            sendCount++;
        }

        public int getSendCount() {
            return sendCount;
        }
    }
}
