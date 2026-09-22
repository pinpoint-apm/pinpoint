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
package com.navercorp.pinpoint.alarm.batch.config;

import com.navercorp.pinpoint.alarm.config.AlarmDaoConfigurationSupport;
import com.navercorp.pinpoint.alarm.dao.AlarmHistoryV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationOutboxDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleLocalConfigDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateItemDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmStateDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryService;
import com.navercorp.pinpoint.alarm.evaluation.ConditionEvaluator;
import com.navercorp.pinpoint.alarm.dao.mapper.AlarmRuleLocalConfigMapper;
import com.navercorp.pinpoint.alarm.dao.mapper.AlarmTemplateItemMapper;
import com.navercorp.pinpoint.alarm.dao.mapper.AlarmRuleV2Mapper;
import com.navercorp.pinpoint.alarm.sender.AlarmNotificationService;
import com.navercorp.pinpoint.alarm.service.AlarmDataSourceProvider;
import com.navercorp.pinpoint.alarm.service.AlarmEvaluationService;
import com.navercorp.pinpoint.alarm.service.AlarmEventPersistenceService;
import com.navercorp.pinpoint.alarm.service.EffectiveAlarmRuleBulkResolutionService;
import com.navercorp.pinpoint.alarm.service.EffectiveAlarmRuleResolver;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.MySQLContainer;

import javax.sql.DataSource;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class AlarmJobIntegrationTestConfig extends AlarmDaoConfigurationSupport {

    @SuppressWarnings("resource") // lifecycle managed by @Container in the test
    public static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.34")
            .withDatabaseName("pinpoint")
            .withUsername("test")
            .withPassword("test");

    // ---- DataSource ----

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(MYSQL.getJdbcUrl());
        config.setDriverClassName(MYSQL.getDriverClassName());
        config.setUsername(MYSQL.getUsername());
        config.setPassword(MYSQL.getPassword());
        config.setMaximumPoolSize(5);
        return new HikariDataSource(config);
    }

    // ---- Schema initialization ----

    @Bean
    public DataSourceInitializer alarmSchemaInitializer(DataSource dataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(false);
        populator.addScript(new ClassPathResource("alarm/sql/CreateExternalTables.sql"));
        // The deployed DDL, not a copy of it: a hand-maintained fixture drifts from the
        // shipped script silently, and the tests that would catch an index or column
        // change are exactly the ones running against the copy.
        populator.addScript(new ClassPathResource("sql/alarm/CreateTableStatement-mysql.sql"));
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    @Bean
    public DataSourceInitializer batchSchemaInitializer(DataSource dataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(true); // tolerate "already exists"
        populator.addScript(new ClassPathResource("org/springframework/batch/core/schema-mysql.sql"));
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    // ---- Transaction / Spring Batch infrastructure ----

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public JobRepository jobRepository(
            DataSource dataSource,
            PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setDatabaseType("MYSQL");
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.afterPropertiesSet();
        return launcher;
    }

    // ---- MyBatis ----

    @Bean
    public FactoryBean<SqlSessionFactory> alarmSqlSessionFactory(DataSource dataSource) throws Exception {
        // Enumerated rather than matched by pattern: a classpath* pattern also picks up the
        // mappers other modules ship, and a non-matching one silently yields none.
        org.springframework.core.io.Resource[] mappers = new org.springframework.core.io.Resource[]{
                new ClassPathResource("alarm/mapper/AlarmRuleV2Mapper.xml"),
                new ClassPathResource("alarm/mapper/AlarmStateMapper.xml"),
                new ClassPathResource("alarm/mapper/AlarmHistoryV2Mapper.xml"),
                new ClassPathResource("alarm/mapper/AlarmNotificationChannelMapper.xml"),
                new ClassPathResource("alarm/mapper/AlarmTemplateMapper.xml"),
                new ClassPathResource("alarm/mapper/AlarmTemplateItemMapper.xml"),
                new ClassPathResource("alarm/mapper/AlarmRuleLocalConfigMapper.xml"),
                new ClassPathResource("alarm/mapper/AlarmChannelBindingMapper.xml"),
                new ClassPathResource("alarm/mapper/AlarmNotificationOutboxMapper.xml"),
        };

        Configuration config = new Configuration();
        config.setCacheEnabled(false);
        config.setUseGeneratedKeys(true);
        config.setMapUnderscoreToCamelCase(true);
        config.setDefaultExecutorType(ExecutorType.SIMPLE);

        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(mappers);
        bean.setConfiguration(config);
        bean.setFailFast(true);
        return bean;
    }

    // ---- DAO beans ----
    // Everything except the ObjectMapper is inherited from AlarmDaoConfigurationSupport,
    // the same class the deployables use, so this config cannot drift from them.

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /** What a deployable contributes; here, the one the test rules point at. */
    @Bean
    public AlarmDataSourceProvider alarmDataSourceProvider() {
        return new IntegrationTestAlarmDataSource.Provider();
    }

    // ---- Mock beans: no real mail, sms or metric backend ----

    @Bean
    public AlarmNotificationService alarmNotificationService() {
        return mock(AlarmNotificationService.class);
    }

    /**
     * Stands in for the metric backend. The tests drive it per case, so what a condition
     * evaluates to is decided by the test rather than by a live query.
     */
    @Bean
    public MetricQueryService metricQueryService() {
        MetricQueryService metricQueryService = mock(MetricQueryService.class);
        when(metricQueryService.getDataSource()).thenReturn(IntegrationTestAlarmDataSource.PRIMARY);
        return metricQueryService;
    }

    // ---- Service ----

    @Bean
    public AlarmEventPersistenceService alarmEventPersistenceService(
            AlarmRuleV2Dao alarmRuleV2Dao,
            AlarmStateDao alarmStateDao,
            AlarmHistoryV2Dao alarmHistoryV2Dao,
            AlarmNotificationOutboxDao alarmNotificationOutboxDao,
            AlarmNotificationService alarmNotificationService,
            ObjectMapper objectMapper,
            org.springframework.transaction.PlatformTransactionManager transactionManager) {
        return new AlarmEventPersistenceService(
                alarmRuleV2Dao, alarmStateDao, alarmHistoryV2Dao, alarmNotificationOutboxDao,
                alarmNotificationService, transactionManager, objectMapper);
    }

    @Bean
    public AlarmEvaluationService alarmEvaluationService(
            AlarmStateDao alarmStateDao,
            AlarmEventPersistenceService alarmEventPersistenceService) {
        return new AlarmEvaluationService(
                alarmStateDao, alarmEventPersistenceService, new ConditionEvaluator());
    }

    @Bean
    public EffectiveAlarmRuleResolver effectiveAlarmRuleResolver() {
        return new EffectiveAlarmRuleResolver();
    }

    @Bean
    public EffectiveAlarmRuleBulkResolutionService effectiveAlarmRuleBulkResolver(
            AlarmRuleLocalConfigDao localConfigDao,
            AlarmTemplateItemDao templateItemDao,
            AlarmTemplateDao templateDao,
            EffectiveAlarmRuleResolver resolver) {
        return new EffectiveAlarmRuleBulkResolutionService(localConfigDao, templateItemDao, templateDao, resolver);
    }

    @Bean
    public ThreadPoolTaskExecutor alarmTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(16);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("alarm-test-");
        return executor;
    }

    // ---- Batch Job ----
    // The shipped configuration, not a copy of it: the step's transaction handling and the
    // tasklet's continuation behaviour are part of what these cases exercise, and a test that
    // rebuilt them here would pass while the real wiring was broken. Only the metric backend
    // and the worker pool are supplied by this class.
    @Bean
    public AlarmJobConfiguration alarmJobConfiguration() {
        return new AlarmJobConfiguration();
    }

    @Bean
    public Tasklet alarmTasklet(AlarmJobConfiguration alarmJobConfiguration,
                                AlarmRuleV2Dao alarmRuleV2Dao,
                                EffectiveAlarmRuleBulkResolutionService effectiveAlarmRuleBulkResolver,
                                AlarmEvaluationService evaluationService,
                                MetricQueryService metricQueryService,
                                @Qualifier("alarmTaskExecutor") ThreadPoolTaskExecutor taskExecutor) {
        return alarmJobConfiguration.alarmTasklet(
                alarmRuleV2Dao,
                effectiveAlarmRuleBulkResolver,
                evaluationService,
                List.<MetricQueryService>of(metricQueryService),
                taskExecutor,
                300
        );
    }

    @Bean
    public Step alarmStep(AlarmJobConfiguration alarmJobConfiguration,
                          JobRepository jobRepository,
                          Tasklet alarmTasklet) {
        return alarmJobConfiguration.alarmStep(jobRepository, alarmTasklet);
    }

    @Bean
    public Job alarmEvaluationJob(AlarmJobConfiguration alarmJobConfiguration,
                                  JobRepository jobRepository,
                                  Step alarmStep) {
        return alarmJobConfiguration.alarmEvaluationJob(jobRepository, alarmStep);
    }
}
