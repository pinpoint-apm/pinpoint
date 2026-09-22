/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.alarm.batch.starter;

import com.navercorp.pinpoint.alarm.config.AlarmDaoConfigurationSupport;
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
 * Binds the alarm mappers to the data source holding the alarm tables.
 *
 * <p>The mapper locations are a compile-time constant so that a pattern matching nothing
 * fails the test that resolves it rather than building an empty SqlSessionFactory whose
 * first DAO call dies far from the configuration that caused it.
 */
@Configuration
public class AlarmBatchDaoConfiguration extends AlarmDaoConfigurationSupport {

    // The same pattern the web resolves. Narrowing it to Alarm* bought nothing -- the
    // alarm-core mappers sit under alarm/core/mapper and were never in reach of it -- and
    // would have dropped any future mapper whose name starts elsewhere from this process
    // alone, which surfaces as Invalid bound statement on a call the web serves fine.
    static final String MAPPER_LOCATION = "classpath*:alarm/mapper/*Mapper.xml";

    @Bean
    public FactoryBean<SqlSessionFactory> alarmSqlSessionFactory(
            @Qualifier("myBatisConfigurationCustomizer") MyBatisConfigurationCustomizer customizer,
            @Qualifier("dataSource") DataSource dataSource,
            @Value(MAPPER_LOCATION) Resource[] mappers) {
        return newAlarmSqlSessionFactory(customizer, dataSource, mappers);
    }
}
