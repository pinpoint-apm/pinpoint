/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.web.service;

import com.navercorp.pinpoint.common.hbase.parallel.ParallelResultScanner;
import com.navercorp.pinpoint.otlp.common.definition.Metric;
import com.navercorp.pinpoint.otlp.common.definition.MetricDefinitionProperty;
import com.navercorp.pinpoint.otlp.common.definition.MetricGroup;
import com.navercorp.pinpoint.otlp.common.definition.Tag;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo-jung
 */
@Service
public class MetricDefinitionServiceImpl implements MetricDefinitionService {
    @Override

    public MetricDefinitionProperty getMetricDefinitionInfo() {

        List<MetricGroup> metricGroupList = new ArrayList<>();
        metricGroupList.add(createMetricGroup1());
        metricGroupList.add(createMetricGroup2());

        List<String> chartTypeList = new ArrayList<>();
        chartTypeList.add("bar");
        chartTypeList.add("areaSpline");
        chartTypeList.add("spline");
        List<String> aggregationFunctionList = new ArrayList<>();
        aggregationFunctionList.add("sum");
        aggregationFunctionList.add("avg");
        aggregationFunctionList.add("min");
        aggregationFunctionList.add("max");

        MetricDefinitionProperty metricDefinitionProperty = new MetricDefinitionProperty(metricGroupList, chartTypeList, aggregationFunctionList);
        return metricDefinitionProperty;
    }

    private MetricGroup createMetricGroup1() {
        List<String> unitList = new ArrayList<>();
        unitList.add("count");
        unitList.add("bytes");

        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("pool:HikariPool-main-dataSource-local,telemetry.sdk.language:java,telemetry.sdk.version:1.12.4,telemetry.sdk.name:io.micrometer", "count"));
        tagList.add(new Tag("name:nodeHistogramAppendExecutor,telemetry.sdk.language:java,telemetry.sdk.version:1.12.4,telemetry.sdk.name:io.micrometer", "bytes"));

        Metric metric  = new Metric("metricId1_1", tagList, unitList);

        List<String> unitList2 = new ArrayList<>();
        unitList2.add("count");
        unitList2.add("bytes");

        List<Tag> tagList2 = new ArrayList<>();
        tagList2.add(new Tag("cache.manager:applicationNameList,cache:applicationNameList,telemetry.sdk.language:java,name:applicationNameList,telemetry.sdk.version:1.12.4,telemetry.sdk.name:io.micrometer", "count"));
        tagList2.add(new Tag("name:serverInfoAppendExecutor,telemetry.sdk.language:java,telemetry.sdk.version:1.12.4,telemetry.sdk.name:io.micrometer", "bytes"));

        Metric metric2  = new Metric("metricId1_2", tagList2, unitList2);

        List<Metric> metricList = new ArrayList<>();
        metricList.add(metric);
        metricList.add(metric2);

        MetricGroup metricGroup = new MetricGroup("metricGroupName1", metricList);
        return metricGroup;
    }

    private MetricGroup createMetricGroup2() {
        List<String> unitList = new ArrayList<>();
        unitList.add("count");
        unitList.add("bytes");

        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("pool:HikariPool-main-dataSource-local,telemetry.sdk.language:java,telemetry.sdk.version:1.12.4,telemetry.sdk.name:io.micrometer", "count"));
        tagList.add(new Tag("name:nodeHistogramAppendExecutor,telemetry.sdk.language:java,telemetry.sdk.version:1.12.4,telemetry.sdk.name:io.micrometer", "bytes"));

        Metric metric  = new Metric("metricId2_1", tagList, unitList);

        List<String> unitList2 = new ArrayList<>();
        unitList2.add("count");
        unitList2.add("bytes");

        List<Tag> tagList2 = new ArrayList<>();
        tagList2.add(new Tag("cache.manager:applicationNameList,cache:applicationNameList,telemetry.sdk.language:java,name:applicationNameList,telemetry.sdk.version:1.12.4,telemetry.sdk.name:io.micrometer", "count"));
        tagList2.add(new Tag("name:serverInfoAppendExecutor,telemetry.sdk.language:java,telemetry.sdk.version:1.12.4,telemetry.sdk.name:io.micrometer", "bytes"));

        Metric metric2  = new Metric("metricId2_2", tagList2, unitList2);

        List<Metric> metricList = new ArrayList<>();
        metricList.add(metric);
        metricList.add(metric2);

        MetricGroup metricGroup = new MetricGroup("metricGroupName2", metricList);
        return metricGroup;
    }
}
