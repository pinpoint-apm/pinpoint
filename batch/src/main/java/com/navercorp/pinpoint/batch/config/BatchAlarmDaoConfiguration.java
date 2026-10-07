/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.batch.config;

import com.navercorp.pinpoint.alarm.config.AlarmDaoConfigurationSupport;
import com.navercorp.pinpoint.alarm.core.service.CoreAlarmDataSourceProvider;
import com.navercorp.pinpoint.mybatis.MyBatisConfigurationCustomizer;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

/**
 * The alarm DAOs, for the application cleanup that deletes the alarm rules of a deleted application.
 */
@Configuration
public class BatchAlarmDaoConfiguration extends AlarmDaoConfigurationSupport {

    static final String MAPPER_LOCATION = "classpath*:alarm/mapper/*Mapper.xml";

    @Bean
    public FactoryBean<SqlSessionFactory> alarmSqlSessionFactory(
            @Qualifier("myBatisConfigurationCustomizer") MyBatisConfigurationCustomizer customizer,
            @Qualifier("dataSource") DataSource dataSource,
            @Value(MAPPER_LOCATION) Resource[] mappers) {
        return newAlarmSqlSessionFactory(customizer, dataSource, mappers);
    }

    @Bean
    public CoreAlarmDataSourceProvider coreAlarmDataSourceProvider() {
        return new CoreAlarmDataSourceProvider();
    }
}
