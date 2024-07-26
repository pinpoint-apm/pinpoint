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

package com.navercorp.pinpoint.otlp.web.dao.mysql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.JsonRuntimeException;
import com.navercorp.pinpoint.otlp.common.defined.AppMetricDefinition;
import com.navercorp.pinpoint.otlp.web.dao.AppMetricDefinitionDao;
import com.navercorp.pinpoint.otlp.web.dao.model.AppMetricDefDto;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo-jung
 */
@Repository
public class MysqlAppMetricDefinitionDao implements AppMetricDefinitionDao {

    private static final String NAMESPACE = AppMetricDefinitionDao.class.getName() + ".";
    private final SqlSessionTemplate sqlSessionTemplate;
    private final Mapper mapper;

    public MysqlAppMetricDefinitionDao(@Qualifier("otlpMysqlSqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate, ObjectMapper mapper) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.mapper = new Mapper(mapper);
    }

    @Override
    public void insertAppMetricDefinitionList(List<AppMetricDefinition> appMetricDefinitionList) {
        AppMetricDefDto appMetricDefDto = mapper.toDto(appMetricDefinitionList);
        sqlSessionTemplate.insert(NAMESPACE + "insertAppMetricDefinition", appMetricDefDto);
    }

    @Override
    public List<AppMetricDefinition> selectAppMetricDefinitionList(String applicationName) {
        AppMetricDefDto appMetricDefDto = sqlSessionTemplate.selectOne(NAMESPACE + "selectAppMetricDefinition", applicationName);
        return mapper.toModel(appMetricDefDto);
    }

    @Override
    public void updateAppMetricDefinitionList(List<AppMetricDefinition> appMetricDefinitionList) {
        AppMetricDefDto appMetricDefDto = mapper.toDto(appMetricDefinitionList);
        int result = sqlSessionTemplate.update(NAMESPACE + "updateAppMetricDefinition", appMetricDefDto);
        System.out.println("result = " + result);
    }

    static class Mapper {
        private final ObjectMapper mapper;
        private final TypeReference<List<AppMetricDefinition>> REF_LIST_APP_METRIC_DEFINITION = new TypeReference<>() {};

        public Mapper(ObjectMapper mapper) {
            this.mapper = Objects.requireNonNull(mapper, "mapper");
        }

        public AppMetricDefDto toDto(List<AppMetricDefinition> appMetricDefinitionList) {
            Objects.requireNonNull(appMetricDefinitionList, "appMetricDefinitionList");

            String applicationName = appMetricDefinitionList.get(0).getApplicationName();

            try {
                String metricConfigJson = mapper.writeValueAsString(appMetricDefinitionList);
                return new AppMetricDefDto(applicationName, metricConfigJson, AppMetricDefinition.SCHEMA_VERSION);
            } catch (JsonProcessingException e) {
                throw new JsonRuntimeException("can not convert appMetricDefinitionList to json :" + appMetricDefinitionList, e);
            }
        }

        public List<AppMetricDefinition> toModel(AppMetricDefDto appMetricDefDto) {
            try {
                List<AppMetricDefinition> appMetricDefinitionList = mapper.readValue(appMetricDefDto.metricDefinition(), REF_LIST_APP_METRIC_DEFINITION);
                return appMetricDefinitionList;
            } catch (JsonProcessingException e) {
                throw new JsonRuntimeException("can not convert appMetricDefDto to model :" + appMetricDefDto, e);
            }
        }
    }
}
