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
package com.navercorp.pinpoint.alarm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.dao.AlarmChannelBindingDao;
import com.navercorp.pinpoint.alarm.dao.AlarmHistoryV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationChannelDao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationOutboxDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleLocalConfigDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleLocalConfigDaoImpl;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2DaoImpl;
import com.navercorp.pinpoint.alarm.dao.AlarmStateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateItemDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateItemDaoImpl;
import com.navercorp.pinpoint.alarm.dao.entity.AlarmRuleEntityMapper;
import com.navercorp.pinpoint.alarm.dao.mapper.AlarmRuleLocalConfigMapper;
import com.navercorp.pinpoint.alarm.dao.mapper.AlarmRuleV2Mapper;
import com.navercorp.pinpoint.alarm.dao.mapper.AlarmTemplateItemMapper;
import com.navercorp.pinpoint.alarm.service.AlarmDataSourceProvider;
import com.navercorp.pinpoint.alarm.service.AlarmDataSourceRegistry;
import com.navercorp.pinpoint.mybatis.MyBatisConfigurationCustomizer;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

import java.util.List;

/**
 * The alarm DAO beans, shared by every deployable that talks to the alarm tables, plus
 * the data source registry every one of them resolves rules through.
 *
 * <p>Subclasses are the {@code @Configuration} classes; each one only declares
 * {@link #alarmSqlSessionFactory} so it can name its own MyBatis customizer and
 * mapper location, and inherits the rest. Adding a DAO to the shared jar used to
 * mean editing every deployable's configuration by hand, and missing one showed up
 * as a startup failure in that deployable alone.
 */
public abstract class AlarmDaoConfigurationSupport {

    /**
     * Builds the session factory the inherited beans qualify on. Each subclass declares
     * the {@code alarmSqlSessionFactory} bean itself, because the customizer qualifier
     * and the mapper location differ per deployable, and the integration test builds one
     * against Testcontainers instead.
     */
    protected FactoryBean<SqlSessionFactory> newAlarmSqlSessionFactory(
            MyBatisConfigurationCustomizer customizer, DataSource dataSource, Resource[] mappers) {

        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setMapperLocations(mappers);

        Configuration config = new Configuration();
        customizer.customize(config);

        sessionFactoryBean.setConfiguration(config);
        sessionFactoryBean.setFailFast(true);

        return sessionFactoryBean;
    }

    @Bean
    public SqlSessionTemplate alarmSqlSessionTemplate(
            @Qualifier("alarmSqlSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }

    @Bean
    public MapperFactoryBean<AlarmRuleV2Mapper> alarmRuleV2Mapper(
            @Qualifier("alarmSqlSessionFactory") SqlSessionFactory sessionFactory) {
        MapperFactoryBean<AlarmRuleV2Mapper> factory = new MapperFactoryBean<>(AlarmRuleV2Mapper.class);
        factory.setSqlSessionFactory(sessionFactory);
        return factory;
    }

    @Bean
    public MapperFactoryBean<AlarmTemplateItemMapper> alarmTemplateItemMapper(
            @Qualifier("alarmSqlSessionFactory") SqlSessionFactory sessionFactory) {
        MapperFactoryBean<AlarmTemplateItemMapper> factory = new MapperFactoryBean<>(AlarmTemplateItemMapper.class);
        factory.setSqlSessionFactory(sessionFactory);
        return factory;
    }

    @Bean
    public MapperFactoryBean<AlarmTemplateDao> alarmTemplateDao(
            @Qualifier("alarmSqlSessionFactory") SqlSessionFactory sessionFactory) {
        MapperFactoryBean<AlarmTemplateDao> factory = new MapperFactoryBean<>(AlarmTemplateDao.class);
        factory.setSqlSessionFactory(sessionFactory);
        return factory;
    }

    @Bean
    public MapperFactoryBean<AlarmRuleLocalConfigMapper> alarmRuleLocalConfigMapper(
            @Qualifier("alarmSqlSessionFactory") SqlSessionFactory sessionFactory) {
        MapperFactoryBean<AlarmRuleLocalConfigMapper> factory =
                new MapperFactoryBean<>(AlarmRuleLocalConfigMapper.class);
        factory.setSqlSessionFactory(sessionFactory);
        return factory;
    }

    @Bean
    public AlarmRuleEntityMapper alarmRuleEntityMapper(ObjectMapper objectMapper) {
        return new AlarmRuleEntityMapper(objectMapper);
    }

    @Bean
    public AlarmRuleV2Dao alarmRuleV2Dao(AlarmRuleV2Mapper mapper, AlarmRuleEntityMapper entityMapper) {
        return new AlarmRuleV2DaoImpl(mapper, entityMapper);
    }

    @Bean
    public AlarmTemplateItemDao alarmTemplateItemDao(
            AlarmTemplateItemMapper mapper, AlarmRuleEntityMapper entityMapper) {
        return new AlarmTemplateItemDaoImpl(mapper, entityMapper);
    }

    @Bean
    public AlarmRuleLocalConfigDao alarmRuleLocalConfigDao(
            AlarmRuleLocalConfigMapper mapper, AlarmRuleEntityMapper entityMapper) {
        return new AlarmRuleLocalConfigDaoImpl(mapper, entityMapper);
    }

    @Bean
    public MapperFactoryBean<AlarmStateDao> alarmStateDao(
            @Qualifier("alarmSqlSessionFactory") SqlSessionFactory sessionFactory) {
        MapperFactoryBean<AlarmStateDao> factory = new MapperFactoryBean<>(AlarmStateDao.class);
        factory.setSqlSessionFactory(sessionFactory);
        return factory;
    }

    @Bean
    public MapperFactoryBean<AlarmHistoryV2Dao> alarmHistoryV2Dao(
            @Qualifier("alarmSqlSessionFactory") SqlSessionFactory sessionFactory) {
        MapperFactoryBean<AlarmHistoryV2Dao> factory = new MapperFactoryBean<>(AlarmHistoryV2Dao.class);
        factory.setSqlSessionFactory(sessionFactory);
        return factory;
    }

    @Bean
    public MapperFactoryBean<AlarmNotificationOutboxDao> alarmNotificationOutboxDao(
            @Qualifier("alarmSqlSessionFactory") SqlSessionFactory sessionFactory) {
        MapperFactoryBean<AlarmNotificationOutboxDao> factory =
                new MapperFactoryBean<>(AlarmNotificationOutboxDao.class);
        factory.setSqlSessionFactory(sessionFactory);
        return factory;
    }

    @Bean
    public MapperFactoryBean<AlarmNotificationChannelDao> alarmNotificationChannelDao(
            @Qualifier("alarmSqlSessionFactory") SqlSessionFactory sessionFactory) {
        MapperFactoryBean<AlarmNotificationChannelDao> factory = new MapperFactoryBean<>(AlarmNotificationChannelDao.class);
        factory.setSqlSessionFactory(sessionFactory);
        return factory;
    }

    @Bean
    public MapperFactoryBean<AlarmChannelBindingDao> alarmChannelBindingDao(
            @Qualifier("alarmSqlSessionFactory") SqlSessionFactory sessionFactory) {
        MapperFactoryBean<AlarmChannelBindingDao> factory = new MapperFactoryBean<>(AlarmChannelBindingDao.class);
        factory.setSqlSessionFactory(sessionFactory);
        return factory;
    }

    // The data sources this deployable has installed: each contributing module declares
    // its own AlarmDataSourceProvider, and this indexes them by the code a rule stores.
    // A deployable that declares none fails here rather than rejecting every rule later.
    @Bean
    public AlarmDataSourceRegistry alarmDataSourceRegistry(List<AlarmDataSourceProvider> providers) {
        return new AlarmDataSourceRegistry(providers);
    }
}
