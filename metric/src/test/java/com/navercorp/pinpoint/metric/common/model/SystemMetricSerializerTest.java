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

package com.navercorp.pinpoint.metric.common.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.metric.collector.view.SystemMetricView;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author Hyunjoon Cho
 */
public class SystemMetricSerializerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testLongCounterWithTags() throws JsonProcessingException {
        Tag deviceTag = new Tag("device", "disk1s5");
        Tag fstypeTag = new Tag("fstype", "apfs");
        Tag modeTag = new Tag("mode", "ro");
        Tag pathTag = new Tag("path", "/");
        LongCounter longCounter = new LongCounter("disk", "localhost", "free", 250685575168L,
                Arrays.asList(deviceTag, fstypeTag, modeTag, pathTag), System.currentTimeMillis());
        SystemMetricView systemMetricView = new SystemMetricView("applicationName", longCounter);
        String json = mapper.writeValueAsString(systemMetricView);
        logger.info("{}", json);
    }

    @Test
    public void testLongCounterWithoutTags() throws JsonProcessingException {
        LongCounter longCounter = new LongCounter("mem", "localhost", "free", 103714816L,
                null, System.currentTimeMillis());
        SystemMetricView systemMetricView = new SystemMetricView("applicationName", longCounter);
        String json = mapper.writeValueAsString(systemMetricView);
        logger.info("{}", json);
    }

    @Test
    public void testDoubleCounter() throws JsonProcessingException {
        Tag cpuTag = new Tag("cpu", "cpu0");
        DoubleCounter doubleCounter = new DoubleCounter("cpu", "localhost", "usage_user", 16.200000000001854,
                Arrays.asList(cpuTag), System.currentTimeMillis());
        SystemMetricView systemMetricView = new SystemMetricView("applicationName", doubleCounter);
        String json = mapper.writeValueAsString(systemMetricView);
        logger.info("{}", json);
    }
}
