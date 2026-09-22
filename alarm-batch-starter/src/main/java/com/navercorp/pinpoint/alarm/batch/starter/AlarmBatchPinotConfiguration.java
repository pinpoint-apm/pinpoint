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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.core.agentstat.dao.AgentStatAlarmDao;
import com.navercorp.pinpoint.alarm.core.agentstat.dao.pinot.AgentStatRegistryHandler;
import com.navercorp.pinpoint.alarm.core.agentstat.dao.pinot.PinotAgentStatAlarmDao;
import com.navercorp.pinpoint.common.server.metric.dao.TableNameManager;
import com.navercorp.pinpoint.mybatis.MyBatisConfigurationCustomizer;
import com.navercorp.pinpoint.pinot.config.PinotConfiguration;
import com.navercorp.pinpoint.pinot.mybatis.PinotAsyncTemplate;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

/**
 * The pinot side of what a rule can measure: what the agents of an application reported about
 * themselves.
 *
 * <p>How many tables those samples are spread over is a property of the installation that
 * collected them, so it is read from configuration here and handed to the dao rather than
 * being known by it.
 */
@org.springframework.context.annotation.Configuration
@Import({AlarmBatchPropertySources.class, PinotConfiguration.class})
public class AlarmBatchPinotConfiguration {

    private static final Logger logger = LogManager.getLogger(AlarmBatchPinotConfiguration.class);

    @Bean
    public AgentStatRegistryHandler agentStatRegistryHandler(ObjectMapper objectMapper) {
        return new AgentStatRegistryHandler(objectMapper);
    }

    @Bean
    public FactoryBean<SqlSessionFactory> alarmPinotSessionFactory(
            AgentStatRegistryHandler agentStatRegistryHandler,
            @Qualifier("pinotConfigurationCustomizer") MyBatisConfigurationCustomizer customizer,
            @Qualifier("pinotDataSource") DataSource dataSource,
            @Value(PinotAgentStatAlarmDao.MAPPER_LOCATION) Resource[] mappers) {

        for (Resource mapper : mappers) {
            logger.info("Mapper location: {}", mapper.getDescription());
        }

        Configuration config = new Configuration();
        customizer.customize(config);
        agentStatRegistryHandler.registerHandlers(config);

        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setConfiguration(config);
        sessionFactoryBean.setMapperLocations(mappers);
        sessionFactoryBean.setFailFast(true);
        // Pinot is read-only here, so the session must not try to manage a transaction.
        sessionFactoryBean.setTransactionFactory(new ManagedTransactionFactory());
        return sessionFactoryBean;
    }

    @Bean
    public PinotAsyncTemplate batchPinotAsyncTemplate(
            @Qualifier("alarmPinotSessionFactory") SqlSessionFactory sessionFactory) {
        return new PinotAsyncTemplate(sessionFactory);
    }

    @Bean
    public SqlSessionTemplate batchPinotTemplate(
            @Qualifier("alarmPinotSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }

    /**
     * The table layout carries no default, here or in the shipped properties: it has to match
     * what the collector was configured to write, and a process that guesses reads one table
     * out of however many there are -- the applications that hash elsewhere return nothing
     * and their rules never fire, with no error to say so. Not starting is the louder answer.
     * See alarm-batch-root.properties.
     */
    @Bean
    public AgentStatAlarmDao agentStatAlarmDao(
            PinotAsyncTemplate pinotAsyncTemplate,
            @Qualifier("batchPinotTemplate") SqlSessionTemplate pinotTemplate,
            @Value("${alarm.agent-stat.table.prefix}") String tablePrefix,
            @Value("${alarm.agent-stat.table.padding-length}") int tablePaddingLength,
            @Value("${alarm.agent-stat.table.count}") int tableCount) {
        TableNameManager tableNameManager =
                new TableNameManager(tablePrefix, tablePaddingLength, tableCount);
        return new PinotAgentStatAlarmDao(pinotAsyncTemplate, pinotTemplate, tableNameManager);
    }
}
