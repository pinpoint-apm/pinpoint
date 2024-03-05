/*
 *
 *  * Copyright 2024 NAVER Corp.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.navercorp.pinpoint.batch.alarm.collector.pinot;

import com.navercorp.pinpoint.batch.alarm.collector.DataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.DataSourceDataGetter;
import com.navercorp.pinpoint.batch.alarm.dao.AlarmDao;
import com.navercorp.pinpoint.batch.alarm.vo.AgentFieldUsage;
import com.navercorp.pinpoint.batch.alarm.vo.DataSourceAlarmVO;
import com.navercorp.pinpoint.common.model.TagInformation;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author minwoo-jung
 */
public class DataSourceDataCollector extends DataCollector implements DataSourceDataGetter {

    private final Logger logger = LogManager.getLogger(DataSourceDataCollector.class);
    private static final String EMTPY_STRING = "";
    private final static String METRIC_NAME = "dataSource";
    private final static String FIELD_ACTIVE_CONNECTION = "activeConnectionSize";
    private final static String FIELD_MAX_CONNECTION = "maxConnectionSize";
    private final static String JDBC_URL = "jdbcUrl";
    private final static String ID = "id";
    private final static String DATABASE_NAME = "databaseName";
    private final AlarmDao alarmDao;
    private final Application application;
    private final List<String> agentIds;
    private final long timeSlotEndTime;
    private final long slotInterval;
    private final List<String> fieldList;
    private final MultiValueMap<String, DataSourceAlarmVO> agentDataSourceConnectionUsageRateMap = new LinkedMultiValueMap<>();

    public DataSourceDataCollector(DataCollectorCategory dataCollectorCategory, AlarmDao alarmDao, Application application, List<String> agentIds, long timeSlotEndTime, long slotInterval) {
        super(dataCollectorCategory);
        Objects.requireNonNull(dataCollectorCategory, "dataCollectorCategory");
        this.alarmDao = Objects.requireNonNull(alarmDao, "alarmDao");
        this.application = Objects.requireNonNull(application, "application");
        this.agentIds = Objects.requireNonNull(agentIds, "agentIds");
        this.fieldList = new ArrayList<>(2);
        fieldList.add(FIELD_ACTIVE_CONNECTION);
        fieldList.add(FIELD_MAX_CONNECTION);
        this.timeSlotEndTime = timeSlotEndTime;
        this.slotInterval = slotInterval;
    }

    @Override
    public void collect() {
        Range range = Range.newUncheckedRange(timeSlotEndTime - slotInterval, timeSlotEndTime);
        try {
            Map<String, List<TagInformation>> agentTagInformation = getAgentTagInformation(range);

            List<QueryResult<AgentFieldUsage, TagInformation>> queryResults = new ArrayList<>();

            for (Map.Entry<String, List<TagInformation>> entry : agentTagInformation.entrySet()) {
                String agentId = entry.getKey();
                List<TagInformation> tagInformationList = entry.getValue();

                for (TagInformation tagInformation : tagInformationList) {
                    CompletableFuture<List<AgentFieldUsage>> futureAgent = alarmDao
                            .selectAvgGroupByField(application.getName(), agentId, METRIC_NAME, fieldList, tagInformation.tags(), range);
                    queryResults.add(new QueryResult<>(futureAgent, tagInformation));
                }
            }

            for (QueryResult<AgentFieldUsage, TagInformation> result : queryResults) {
                CompletableFuture<List<AgentFieldUsage>> futureAgent = result.future();
                TagInformation tagInformation = result.key();
                List<AgentFieldUsage> AgentFieldUsageList = futureAgent.get();

                int id = 0;
                String databaseName = EMTPY_STRING;
                int activeConnection = 0;
                int maxConnection = 0;

                for (AgentFieldUsage agentFieldUsage : AgentFieldUsageList) {
                    String fieldName = agentFieldUsage.getFieldName();

                    switch (fieldName) {
                        case FIELD_ACTIVE_CONNECTION:
                            activeConnection = (int) Math.floor(agentFieldUsage.getValue());
                            break;
                        case FIELD_MAX_CONNECTION:
                            maxConnection = (int) Math.floor(agentFieldUsage.getValue());
                            break;
                    }

                    for (Tag tag : tagInformation.tags()){
                        String tagKey = tag.getName();
                        switch (tagKey) {
                            case ID:
                                id = Integer.parseInt(tag.getValue());
                                break;
                            case DATABASE_NAME:
                                databaseName = tag.getValue();
                                break;
                        }
                    }
                }

                DataSourceAlarmVO dataSourceAlarmVO = new DataSourceAlarmVO(id, databaseName, activeConnection, maxConnection);
                agentDataSourceConnectionUsageRateMap.add(tagInformation.agentId(), dataSourceAlarmVO);
            }
        } catch (Exception e) {
            logger.error("Fail to get agent datasource data. applicationName : {}", application.getName(), e);
        }
    }

    private Map<String, List<TagInformation>> getAgentTagInformation(Range range) throws ExecutionException, InterruptedException {
        Map<String, List<Tag>> agentJdbcUrlMap = distinctJdbUrlForAgent(range);
        List<QueryResult<TagInformation, String>> queryResults = new ArrayList<>();

        for (Map.Entry<String, List<Tag>> entry : agentJdbcUrlMap.entrySet()) {
            String agentId = entry.getKey();
            List<Tag> jdbcUrlList = entry.getValue();

            for (Tag jdbcUrlTag : jdbcUrlList) {
                List<Tag> tagList = new ArrayList<>();
                tagList.add(jdbcUrlTag);
                CompletableFuture<List<TagInformation>> futureTagInformation = alarmDao.getTagInfoContainedSpecificTag(application.getName(), agentId, METRIC_NAME, FIELD_ACTIVE_CONNECTION, tagList, range);
                queryResults.add(new QueryResult<>(futureTagInformation, agentId));
            }
        }

        Map<String, List<TagInformation>> agentTagInformationMap = new HashMap<>();

        for (QueryResult<TagInformation, String> result : queryResults) {
            String agentId = result.key();
            CompletableFuture<List<TagInformation>> futureTagInformation = result.future();

            List<TagInformation> tagInfoList = futureTagInformation.get();
            TagInformation tagInformation = tagInfoList.get(0);

            List<TagInformation> tagInformationList = agentTagInformationMap.computeIfAbsent(agentId, k -> new ArrayList<>());
            tagInformationList.add(tagInformation);
        }

        return agentTagInformationMap;
    }

    private Map<String, List<Tag>> distinctJdbUrlForAgent(Range range) throws ExecutionException, InterruptedException {
        List<QueryResult<Tag, String>> queryResults = new ArrayList<>();

        for (String agentId : agentIds) {
            CompletableFuture<List<Tag>> future = alarmDao.selectTagInfo(application.getName(), agentId, METRIC_NAME, FIELD_ACTIVE_CONNECTION, range);
            queryResults.add(new QueryResult<>(future, agentId));
        }

        Map<String, List<Tag>> agentJdbcUrlMap = new HashMap<>();

        for (QueryResult<Tag, String> result : queryResults) {
            CompletableFuture<List<Tag>> futureTag = result.future();
            String agentId = result.key();
            List<Tag> tagList = futureTag.get();
            List<Tag> jdbcUrlList = new ArrayList<>();

            for (Tag tag : tagList) {
                if (tag.getName().equals(JDBC_URL)) {
                    jdbcUrlList.add(tag);
                }
            }

            agentJdbcUrlMap.put(agentId, jdbcUrlList);
        }

        return agentJdbcUrlMap;
    }


    @Override
    public Map<String, List<DataSourceAlarmVO>> getDataSourceConnectionUsageRate() {
        return agentDataSourceConnectionUsageRateMap;
    }

    private record QueryResult<E, K>(CompletableFuture<List<E>> future, K key) {
    }
}