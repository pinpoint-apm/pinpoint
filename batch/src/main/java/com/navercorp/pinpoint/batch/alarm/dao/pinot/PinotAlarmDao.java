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

package com.navercorp.pinpoint.batch.alarm.dao.pinot;

import com.navercorp.pinpoint.batch.alarm.dao.AlarmDao;
import com.navercorp.pinpoint.batch.alarm.dao.model.BatchQueryParameter;
import com.navercorp.pinpoint.batch.alarm.vo.AgentFieldUsage;
import com.navercorp.pinpoint.batch.alarm.vo.AgentUsage;
import com.navercorp.pinpoint.batch.alarm.vo.AgentUsageCount;
import com.navercorp.pinpoint.common.model.TagInformation;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.pinot.mybatis.PinotAsyncTemplate;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Retryable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author minwoo-jung
 */
@Retryable(maxAttempts = 3, value = {Exception.class})
public class PinotAlarmDao implements AlarmDao {

    private static final String NAMESPACE = PinotAlarmDao.class.getName() + ".";

    private final PinotAsyncTemplate asyncTemplate;
    private final SqlSessionTemplate syncTemplate;

    public PinotAlarmDao(@Qualifier("batchPinotAsyncTemplate") PinotAsyncTemplate asyncTemplate, @Qualifier("batchPinotTemplate") SqlSessionTemplate syncTemplate) {
        this.asyncTemplate = Objects.requireNonNull(asyncTemplate, "asyncTemplate");
        this.syncTemplate = Objects.requireNonNull(syncTemplate, "syncTemplate");
    }

    @Override
    public List<AgentFieldUsage> selectSumGroupByField(String applicationName, String metricName, List<String> fieldList, Range range) {
        BatchQueryParameter batchQueryParameter = new BatchQueryParameter(applicationName, metricName, fieldList, range);
        return syncTemplate.selectList(NAMESPACE + "selectSumGroupByField", batchQueryParameter);
    }

    @Override
    public CompletableFuture<List<AgentFieldUsage>> selectAvgGroupByField(String applicationName, String agentId, String metricName, List<String> fieldList, List<Tag> tagList, Range range) {
        BatchQueryParameter batchQueryParameter = new BatchQueryParameter(applicationName, agentId, metricName, fieldList, tagList, range);
        return asyncTemplate.selectList(NAMESPACE + "selectAvgGroupByField", batchQueryParameter);
    }

    @Override
    public List<AgentUsageCount> selectSumCount(String applicationName, String metricName, String fieldName, Range range) {
        BatchQueryParameter batchQueryParameter = new BatchQueryParameter(applicationName, metricName, fieldName, range);
        return syncTemplate.selectList(NAMESPACE + "selectSumCount", batchQueryParameter);
    }

    @Override
    public List<AgentUsage> selectAvg(String applicationName, String metricName, String fieldName, Range range) {
        BatchQueryParameter batchQueryParameter = new BatchQueryParameter(applicationName, metricName, fieldName, range);
        return syncTemplate.selectList(NAMESPACE + "selectAvg", batchQueryParameter);
    }

    @Override
    public CompletableFuture<List<Tag>> selectTagInfo(String applicationName, String agentId, String metricName, String fieldName, Range range) {
        BatchQueryParameter batchQueryParameter = new BatchQueryParameter(applicationName, agentId, metricName, fieldName, range);
        return asyncTemplate.selectList(NAMESPACE + "selectTagInfo", batchQueryParameter);
    }

    @Override
    public CompletableFuture<List<TagInformation>> getTagInfoContainedSpecificTag(String applicationName, String agentId, String metricName, String fieldName, List<Tag> tagList, Range range) {
        BatchQueryParameter batchQueryParameter = new BatchQueryParameter(applicationName, agentId, metricName, fieldName, tagList, range);
        return asyncTemplate.selectList(NAMESPACE + "selectTagInfoContainedSpecificTag", batchQueryParameter);
    }
}
