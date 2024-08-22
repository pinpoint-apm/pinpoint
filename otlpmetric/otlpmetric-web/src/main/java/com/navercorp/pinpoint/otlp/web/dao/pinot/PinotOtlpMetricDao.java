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

package com.navercorp.pinpoint.otlp.web.dao.pinot;

import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSampler;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.otlp.common.model.DataType;
import com.navercorp.pinpoint.otlp.common.model.MetricPoint;
import com.navercorp.pinpoint.otlp.web.dao.OtlpMetricDao;
import com.navercorp.pinpoint.otlp.web.view.legacy.OtlpChartView;
import com.navercorp.pinpoint.otlp.web.view.legacy.OtlpChartViewBuilder;
import com.navercorp.pinpoint.otlp.web.vo.*;
import com.navercorp.pinpoint.pinot.mybatis.PinotAsyncTemplate;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Repository
public class PinotOtlpMetricDao implements OtlpMetricDao {
    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(30000L, 200);
    public static final OtlpChartView EMPTY_CHART = OtlpChartViewBuilder.EMPTY_CHART_VIEW;
    private static final String NAMESPACE = PinotOtlpMetricDao.class.getName() + ".";
    private final SqlSessionTemplate syncTemplate;
    private final PinotAsyncTemplate asyncTemplate;

    public PinotOtlpMetricDao(@Qualifier("otlpMetricPinotSessionTemplate") SqlSessionTemplate syncTemplate,
                              @Qualifier("otlpMetricPinotAsyncTemplate") PinotAsyncTemplate asyncTemplate) {
        this.syncTemplate = Objects.requireNonNull(syncTemplate, "syncTemplate");
        this.asyncTemplate = Objects.requireNonNull(asyncTemplate, "asyncTemplate");
    }

    @Deprecated
    @Override
    public List<String> getMetricGroups(String tenantId, String serviceId, String applicationId, String agentId) {
        List<String> metricGroups = this.syncTemplate.selectList(NAMESPACE + "getMetricGroups",
                new OtlpMetricGroupsQueryParam(serviceId, applicationId, agentId));
        return metricGroups;
    }

    @Deprecated
    @Override
    public List<String> getMetrics(String tenantId, String serviceId, String applicationId, String agentId, String metricGroupName) {
        // TODO:
        // 1. Check mysql if metrics to be displayed are specified
        // 2. if no data in mysql, query pinot to get all metric names
        List<String> metrics = this.syncTemplate.selectList(NAMESPACE + "getMetricNames",
                new OtlpMetricNamesQueryParam(serviceId, applicationId, agentId, metricGroupName));
        return metrics;
    }

    @Deprecated
    @Override
    public List<String> getTags(String tenantId, String serviceId, String applicationId, String agentId, String metricGroupName, String metricName) {
        OtlpMetricDetailsQueryParam queryParam = new OtlpMetricDetailsQueryParam(serviceId, applicationId, agentId, metricGroupName, metricName, null);

        List<String> tags = this.syncTemplate.selectList(NAMESPACE + "getTags", queryParam);
        return tags;
    }

    @Deprecated
    @Override
    public List<FieldAttribute> getFields(String serviceId, String applicationId, String agentId, String metricGroupName, String metricName, String tag) {
        OtlpMetricDetailsQueryParam queryParam = new OtlpMetricDetailsQueryParam(serviceId, applicationId, agentId, metricGroupName, metricName, tag);
        return this.syncTemplate.selectList(NAMESPACE + "getFields", queryParam);
    }

    @Override
    public List<FieldAttribute> getFields(String serviceId, String applicationId, String agentId, String metricGroupName, String metricName, String tag, List<String> fieldNameList) {
        OtlpMetricDetailsQueryParam queryParam = new OtlpMetricDetailsQueryParam(serviceId, applicationId, agentId, metricGroupName, metricName, fieldNameList, tag);
        return this.syncTemplate.selectList(NAMESPACE + "getFields", queryParam);
    }


    @Deprecated
    @Override
    public CompletableFuture<List<OtlpMetricChartResult>> getChartPoints(OtlpMetricChartQueryParameter chartQueryParameter) {
        if (chartQueryParameter.getDataType() == DataType.LONG) {
            return asyncTemplate.selectList(NAMESPACE + "getLongChartData", chartQueryParameter);
        } else {
            return asyncTemplate.selectList(NAMESPACE + "getDoubleChartData", chartQueryParameter);
        }
    }

    @Override
    public CompletableFuture<List<MetricPoint>> getChartPoints(OtlpMetricDataQueryParameter chartQueryParameter) {
        if (chartQueryParameter.getDataType() == DataType.LONG) {
            return asyncTemplate.selectList(NAMESPACE + "getLongMetricData", chartQueryParameter);
        } else {
            return asyncTemplate.selectList(NAMESPACE + "getDoubleMetricData", chartQueryParameter);
        }
    }

    @Override
    public String getSummary(OtlpMetricChartQueryParameter chartQueryParameter) {
        if (chartQueryParameter.getDataType() == DataType.LONG) {
            return syncTemplate.selectOne(NAMESPACE + "getLongSummary", chartQueryParameter);
        } else {
            return syncTemplate.selectOne(NAMESPACE + "getDoubleSummary", chartQueryParameter);
        }
    }


}
