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
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.navercorp.pinpoint.common.server.util.json.JsonRuntimeException;
import com.navercorp.pinpoint.otlp.common.web.defined.AppMetricDefinition;
import com.navercorp.pinpoint.otlp.web.dao.AppMetricDefinitionDao;
import com.navercorp.pinpoint.otlp.web.dao.model.AppMetricDefDto;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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
    public void insertAppMetricDefinitionList(String applicationName, List<AppMetricDefinition> appMetricDefinitionList) {
        AppMetricDefDto appMetricDefDto = mapper.toDto(applicationName, appMetricDefinitionList);
        sqlSessionTemplate.insert(NAMESPACE + "insertAppMetricDefinition", appMetricDefDto);
    }

    @Override
    public List<AppMetricDefinition> selectAppMetricDefinitionList(String applicationName) {
        AppMetricDefDto appMetricDefDto = sqlSessionTemplate.selectOne(NAMESPACE + "selectAppMetricDefinition", applicationName);
        return mapper.toModel(appMetricDefDto);
    }

    @Override
    public void updateAppMetricDefinitionList(String applicationName, List<AppMetricDefinition> appMetricDefinitionList) {
        AppMetricDefDto appMetricDefDto = mapper.toDto(applicationName, appMetricDefinitionList);
        sqlSessionTemplate.update(NAMESPACE + "updateAppMetricDefinition", appMetricDefDto);
    }

    static class Mapper {
        private final ObjectWriter writer;
        private final ObjectReader reader;

        public Mapper(ObjectMapper mapper) {
            this.writer = mapper.writerFor(new TypeReference<List<AppMetricDefinition>>() {});
            this.reader = mapper.readerForListOf(AppMetricDefinition.class);
        }

        public AppMetricDefDto toDto(String applicationName, List<AppMetricDefinition> appMetricDefinitionList) {
            Objects.requireNonNull(appMetricDefinitionList, "appMetricDefinitionList");
            try {
                String metricConfigJson = writer.writeValueAsString(appMetricDefinitionList);
                return new AppMetricDefDto(applicationName, metricConfigJson, AppMetricDefinition.SCHEMA_VERSION);
            } catch (JsonProcessingException e) {
                throw new JsonRuntimeException("can not convert appMetricDefinitionList to json :" + appMetricDefinitionList, e);
            }
        }

        public List<AppMetricDefinition> toModel(AppMetricDefDto appMetricDefDto) {
            if (appMetricDefDto == null) {
                return new ArrayList<>();
            }

            try {
                return reader.readValue(appMetricDefDto.metricDefinition());
            } catch (JsonProcessingException e) {
                throw new JsonRuntimeException("can not convert appMetricDefDto to model :" + appMetricDefDto, e);
            }
        }
    }
}
