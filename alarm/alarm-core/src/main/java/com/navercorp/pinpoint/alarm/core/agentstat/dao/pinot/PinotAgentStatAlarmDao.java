/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.alarm.core.agentstat.dao.pinot;

import com.navercorp.pinpoint.alarm.core.agentstat.dao.AgentStatAlarmDao;
import com.navercorp.pinpoint.alarm.core.agentstat.dao.model.AgentStatQueryParameter;
import com.navercorp.pinpoint.alarm.core.agentstat.vo.AgentFieldUsage;
import com.navercorp.pinpoint.alarm.core.agentstat.vo.AgentUsage;
import com.navercorp.pinpoint.alarm.core.agentstat.vo.AgentUsageCount;
import com.navercorp.pinpoint.common.model.TagInformation;
import com.navercorp.pinpoint.common.server.metric.dao.TableNameManager;
import com.navercorp.pinpoint.common.timeseries.time.Range;
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
@Retryable(maxAttempts = 3, retryFor = {Exception.class})
public class PinotAgentStatAlarmDao implements AgentStatAlarmDao {

    /**
     * A compile-time constant so the pattern a deployable resolves is the one the test checks:
     * a pattern that matches nothing builds an empty SqlSessionFactory rather than failing, and
     * the first query then dies far from the configuration that caused it.
     */
    public static final String MAPPER_LOCATION = "classpath*:/alarm/core/mapper/*Mapper.xml";

    private static final String NAMESPACE = PinotAgentStatAlarmDao.class.getName() + ".";

    private final PinotAsyncTemplate asyncTemplate;
    private final SqlSessionTemplate syncTemplate;
    private final TableNameManager tableNameManager;

    /**
     * The table naming is passed in rather than read here: how many tables the agent stats are
     * spread over is a property of the installation that collected them, and this module is
     * used by more than one deployable.
     */
    public PinotAgentStatAlarmDao(@Qualifier("batchPinotAsyncTemplate") PinotAsyncTemplate asyncTemplate,
                                  @Qualifier("batchPinotTemplate") SqlSessionTemplate syncTemplate,
                                  TableNameManager tableNameManager) {
        this.asyncTemplate = Objects.requireNonNull(asyncTemplate, "asyncTemplate");
        this.syncTemplate = Objects.requireNonNull(syncTemplate, "syncTemplate");
        this.tableNameManager = Objects.requireNonNull(tableNameManager, "tableNameManager");
    }

    @Override
    public List<AgentFieldUsage> selectSumGroupByField(String applicationName, String metricName, List<String> fieldList, Range range) {
        AgentStatQueryParameter queryParameter = new AgentStatQueryParameter(getTableName(applicationName), applicationName, metricName, fieldList, range);
        return syncTemplate.selectList(NAMESPACE + "selectSumGroupByField", queryParameter);
    }

    @Override
    public CompletableFuture<List<AgentFieldUsage>> selectAvgGroupByField(String applicationName, String agentId, String metricName, List<String> fieldList, List<Tag> tagList, Range range) {
        AgentStatQueryParameter queryParameter = new AgentStatQueryParameter(getTableName(applicationName), applicationName, agentId, metricName, fieldList, tagList, range);
        return asyncTemplate.selectList(NAMESPACE + "selectAvgGroupByField", queryParameter);
    }

    @Override
    public List<AgentUsageCount> selectSumCount(String applicationName, String metricName, String fieldName, Range range) {
        AgentStatQueryParameter queryParameter = new AgentStatQueryParameter(getTableName(applicationName), applicationName, metricName, fieldName, range);
        return syncTemplate.selectList(NAMESPACE + "selectSumCount", queryParameter);
    }

    @Override
    public List<AgentUsage> selectAvg(String applicationName, String metricName, String fieldName, Range range) {
        AgentStatQueryParameter queryParameter = new AgentStatQueryParameter(getTableName(applicationName), applicationName, metricName, fieldName, range);
        return syncTemplate.selectList(NAMESPACE + "selectAvg", queryParameter);
    }

    @Override
    public CompletableFuture<List<Tag>> selectTagInfo(String applicationName, String agentId, String metricName, String fieldName, Range range) {
        AgentStatQueryParameter queryParameter = new AgentStatQueryParameter(getTableName(applicationName), applicationName, agentId, metricName, fieldName, range);
        return asyncTemplate.selectList(NAMESPACE + "selectTagInfo", queryParameter);
    }

    @Override
    public CompletableFuture<List<TagInformation>> getTagInfoContainedSpecificTag(String applicationName, String agentId, String metricName, String fieldName, List<Tag> tagList, Range range) {
        AgentStatQueryParameter queryParameter = new AgentStatQueryParameter(getTableName(applicationName), applicationName, agentId, metricName, fieldName, tagList, range);
        return asyncTemplate.selectList(NAMESPACE + "selectTagInfoContainedSpecificTag", queryParameter);
    }

    private String getTableName(String applicationName) {
        return tableNameManager.getTableName(applicationName);
    }
}
