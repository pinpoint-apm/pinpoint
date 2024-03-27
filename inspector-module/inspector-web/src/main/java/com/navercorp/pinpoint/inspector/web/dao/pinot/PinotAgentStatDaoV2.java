/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.web.dao.pinot;

import com.navercorp.pinpoint.common.model.TagInformation;
import com.navercorp.pinpoint.inspector.web.dao.AgentStatDao;
import com.navercorp.pinpoint.inspector.web.dao.model.InspectorQueryParameterV2;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.Field;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.metric.common.model.Tag;
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
 * @author minwoo.jung
 */
@Repository("pinotAgentStatDaoV2")
public class PinotAgentStatDaoV2 implements AgentStatDao {

    private static final String NAMESPACE = PinotAgentStatDaoV2.class.getName() + ".";

    private final PinotAsyncTemplate asyncTemplate;
    private final SqlSessionTemplate syncTemplate;


    public PinotAgentStatDaoV2(@Qualifier("inspectorPinotAsyncTemplate") PinotAsyncTemplate asyncTemplate, @Qualifier("inspectorPinotTemplate") SqlSessionTemplate syncTemplate) {
        this.asyncTemplate = Objects.requireNonNull(asyncTemplate, "asyncTemplate");
        this.syncTemplate = Objects.requireNonNull(syncTemplate, "syncTemplate");
    }

    @Override
    public CompletableFuture<List<SystemMetricPoint<Double>>> selectAgentStatAvg(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field) {
        InspectorQueryParameterV2 inspectorQueryParameter = new InspectorQueryParameterV2(inspectorDataSearchKey, metricName, field.getFieldName(), field.getTags());
        return asyncTemplate.selectList(NAMESPACE + "selectInspectorAvgData", inspectorQueryParameter);
    }

    @Override
    public CompletableFuture<List<SystemMetricPoint<Double>>> selectAgentStatMax(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field) {
        InspectorQueryParameterV2 inspectorQueryParameter = new InspectorQueryParameterV2(inspectorDataSearchKey, metricName, field.getFieldName(), field.getTags());
        return asyncTemplate.selectList(NAMESPACE + "selectInspectorMaxData", inspectorQueryParameter);
    }

    @Override
    public CompletableFuture<List<SystemMetricPoint<Double>>> selectAgentStatSum(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field) {
        InspectorQueryParameterV2 inspectorQueryParameter = new InspectorQueryParameterV2(inspectorDataSearchKey, metricName, field.getFieldName(), field.getTags());
        return asyncTemplate.selectList(NAMESPACE + "selectInspectorSumData", inspectorQueryParameter);
    }

    @Override
    public List<Tag> getTagInfo(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field) {
        InspectorQueryParameterV2 inspectorQueryParameter = new InspectorQueryParameterV2(inspectorDataSearchKey, metricName, field.getFieldName());
        return syncTemplate.selectList(NAMESPACE + "selectTagInfo", inspectorQueryParameter);
    }

    @Override
    public TagInformation getTagInfoContainedSpecificTag(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field, Tag tag) {
        List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(tag);
        InspectorQueryParameterV2 inspectorQueryParameter = new InspectorQueryParameterV2(inspectorDataSearchKey, metricName, field.getFieldName(), tagList);

        return syncTemplate.selectOne(NAMESPACE + "selectTagInfoContainedSpecificTag", inspectorQueryParameter);
    }
}
