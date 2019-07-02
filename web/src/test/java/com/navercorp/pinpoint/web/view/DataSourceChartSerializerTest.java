/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.DataSourceSampler;
import com.navercorp.pinpoint.web.test.util.DataSourceTestUtils;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSource;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.DataSourceChart;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
@RunWith(MockitoJUnitRunner.class)
public class DataSourceChartSerializerTest {

    private static final int MIN_VALUE_OF_MAX_CONNECTION_SIZE = 20;
    private static final int CREATE_TEST_OBJECT_MAX_SIZE = 10;

    private final DataSourceSampler sampler = new DataSourceSampler();

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private ServiceTypeRegistryService serviceTypeRegistryService;

    @Before
    public void setUp() throws Exception {
        when(serviceTypeRegistryService.findServiceType(any(Short.class))).thenReturn(ServiceType.UNKNOWN);
    }

    @Test
    public void serializeTest() throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        TimeWindow timeWindow = new TimeWindow(new Range(currentTimeMillis - 300000, currentTimeMillis));

        List<SampledDataSource> sampledDataSourceList = createSampledDataSourceList(timeWindow);
        DataSourceChart dataSourceChartGroup = new DataSourceChart(timeWindow, sampledDataSourceList, serviceTypeRegistryService);

        String jsonValue = mapper.writeValueAsString(dataSourceChartGroup);
        Map map = mapper.readValue(jsonValue, Map.class);

        Assert.assertTrue(map.containsKey("id"));
        Assert.assertTrue(map.containsKey("jdbcUrl"));
        Assert.assertTrue(map.containsKey("databaseName"));
        Assert.assertTrue(map.containsKey("serviceType"));
        Assert.assertTrue(map.containsKey("charts"));
    }

    private List<SampledDataSource> createSampledDataSourceList(TimeWindow timeWindow) {
        List<SampledDataSource> sampledDataSourceList = new ArrayList<>();

        int maxConnectionSize = RandomUtils.nextInt(MIN_VALUE_OF_MAX_CONNECTION_SIZE, MIN_VALUE_OF_MAX_CONNECTION_SIZE * 2);

        long from = timeWindow.getWindowRange().getFrom();
        long to = timeWindow.getWindowRange().getTo();

        for (long i = from; i < to; i += timeWindow.getWindowSlotSize()) {
            sampledDataSourceList.add(createSampledDataSource(i, maxConnectionSize));
        }

        return sampledDataSourceList;
    }

    private SampledDataSource createSampledDataSource(long timestamp, int maxConnectionSize) {
        int testObjectSize = RandomUtils.nextInt(1, CREATE_TEST_OBJECT_MAX_SIZE);
        List<DataSourceBo> dataSourceBoList = DataSourceTestUtils.createDataSourceBoList(1, testObjectSize, maxConnectionSize);
        return sampler.sampleDataPoints(0, timestamp, dataSourceBoList, null);
    }

}
