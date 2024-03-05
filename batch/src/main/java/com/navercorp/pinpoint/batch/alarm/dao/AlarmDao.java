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

package com.navercorp.pinpoint.batch.alarm.dao;

import com.navercorp.pinpoint.batch.alarm.vo.AgentFieldUsage;
import com.navercorp.pinpoint.batch.alarm.vo.AgentUsage;
import com.navercorp.pinpoint.batch.alarm.vo.AgentUsageCount;
import com.navercorp.pinpoint.common.model.TagInformation;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author minwoo-jung
 */
public interface AlarmDao {

    List<AgentFieldUsage> selectSumGroupByField(String applicationName, String metricName, List<String> fieldList, Range range);

    CompletableFuture<List<AgentFieldUsage>> selectAvgGroupByField(String applicationName, String agentId, String metricName, List<String> fieldList, List<Tag> tagList, Range range);

    List<AgentUsageCount> selectSumCount(String applicationName, String metricName, String fieldName, Range range);

    List<AgentUsage> selectAvg(String applicationName, String metricName, String fieldName, Range range);

    CompletableFuture<List<Tag>> selectTagInfo(String applicationName, String agentId, String metricName, String fieldName, Range range);

    CompletableFuture<List<TagInformation>> getTagInfoContainedSpecificTag(String applicationName, String agentId, String metricName, String fieldActiveConnection, List<Tag> tagList, Range range);
}
