/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.inspector.web.dao.pinot;

import com.navercorp.pinpoint.common.model.TagInformation;
import com.navercorp.pinpoint.inspector.web.dao.ApplicationStatDao;
import com.navercorp.pinpoint.inspector.web.dao.model.InspectorQueryParameter;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.Field;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.chart.AvgMinMaxMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.AvgMinMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.MinMaxMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.pinot.mybatis.PinotAsyncTemplate;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author minwoo-jung
 */
@Repository("pinotApplicationStatDao")
public class PinotApplicationStatDao implements ApplicationStatDao {

    private static final String NAMESPACE = PinotApplicationStatDao.class.getName() + ".";
    private final PinotAsyncTemplate asyncTemplate;
    private final SqlSessionTemplate syncTemplate;

    public PinotApplicationStatDao(@Qualifier("inspectorPinotAsyncTemplate") PinotAsyncTemplate asyncTemplate, @Qualifier("inspectorPinotTemplate") SqlSessionTemplate syncTemplate) {
        this.asyncTemplate = Objects.requireNonNull(asyncTemplate, "asyncTemplate");
        this.syncTemplate = Objects.requireNonNull(syncTemplate, "syncTemplate");
    }

    @Override
    public CompletableFuture<List<AvgMinMaxMetricPoint<Double>>> selectStatAvgMinMax(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field) {
        InspectorQueryParameter inspectorQueryParameter = new InspectorQueryParameter(inspectorDataSearchKey, metricName, field.getFieldName(), field.getTags());
        return asyncTemplate.selectList(NAMESPACE + "selectInspectorAvgMinMaxData", inspectorQueryParameter);
    }

    @Override
    public CompletableFuture<List<MinMaxMetricPoint<Double>>> selectStatMinMax(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field) {
        InspectorQueryParameter inspectorQueryParameter = new InspectorQueryParameter(inspectorDataSearchKey, metricName, field.getFieldName(), field.getTags());
        return asyncTemplate.selectList(NAMESPACE + "selectInspectorMinMaxData", inspectorQueryParameter);
    }

    @Override
    public CompletableFuture<List<SystemMetricPoint<Double>>> selectStatSum(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field) {
        InspectorQueryParameter inspectorQueryParameter = new InspectorQueryParameter(inspectorDataSearchKey, metricName, field.getFieldName(), field.getTags());
        return asyncTemplate.selectList(NAMESPACE + "selectInspectorSumData", inspectorQueryParameter);
    }

    @Override
    public CompletableFuture<List<AvgMinMetricPoint<Double>>> selectStatAvgMin(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field) {
        InspectorQueryParameter inspectorQueryParameter = new InspectorQueryParameter(inspectorDataSearchKey, metricName, field.getFieldName(), field.getTags());
        return asyncTemplate.selectList(NAMESPACE + "selectInspectorAvgMinData", inspectorQueryParameter);
    }

    @Override
    public CompletableFuture<List<SystemMetricPoint<Double>>> selectStatMax(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field) {
        InspectorQueryParameter inspectorQueryParameter = new InspectorQueryParameter(inspectorDataSearchKey, metricName, field.getFieldName(), field.getTags());
        return asyncTemplate.selectList(NAMESPACE + "selectInspectorMaxData", inspectorQueryParameter);
    }

    @Override
    public List<Tag> getTagInfo(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field) {
        InspectorQueryParameter inspectorQueryParameter = new InspectorQueryParameter(inspectorDataSearchKey, metricName, field.getFieldName());
        return syncTemplate.selectList(NAMESPACE + "selectTagInfo", inspectorQueryParameter);
    }

    @Override
    public TagInformation getTagInfoContainedSpecificTag(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field, Tag tag) {
        List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(tag);
        InspectorQueryParameter inspectorQueryParameter = new InspectorQueryParameter(inspectorDataSearchKey, metricName, field.getFieldName(), tagList);

        return syncTemplate.selectOne(NAMESPACE + "selectTagInfoContainedSpecificTag", inspectorQueryParameter);
    }
}
