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

package com.navercorp.pinpoint.otlp.web.config.mysql;

import com.navercorp.pinpoint.mybatis.MyBatisConfigurationCustomizer;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import com.navercorp.pinpoint.mybatis.plugin.BindingLogPlugin;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

/**
 * @author minwoo-jung
 */
@org.springframework.context.annotation.Configuration
public class OtlpMetricWebMysqlDaoConfiguration {

    private final Logger logger = LogManager.getLogger(OtlpMetricWebMysqlDaoConfiguration.class);

    @Bean
    public FactoryBean<SqlSessionFactory> otlpMysqlSqlSessionFactory(
            @Qualifier("myBatisConfigurationCustomizer") MyBatisConfigurationCustomizer customizer,
            @Qualifier("dataSource") DataSource dataSource,
            @Value("classpath*:otlp/web/mapper/mysql/*Mapper.xml") Resource[] mappers,
            BindingLogPlugin bindingLogPlugin) {

        for (Resource mapper : mappers) {
            logger.info("Mapper location: {}", mapper.getDescription());
        }

        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setMapperLocations(mappers);

        Configuration config = new Configuration();

        sessionFactoryBean.setConfiguration(config);
        sessionFactoryBean.setFailFast(true);
        sessionFactoryBean.setPlugins(bindingLogPlugin);

        MyBatisRegistryHandler myBatisRegistryHandler = new OtlpMetricMysqlRegistryHandler();
        myBatisRegistryHandler.registerTypeAlias(config.getTypeAliasRegistry());
        myBatisRegistryHandler.registerTypeHandler(config.getTypeHandlerRegistry());

        return sessionFactoryBean;
    }

    @Bean
    public SqlSessionTemplate otlpMysqlSqlSessionTemplate(
            @Qualifier("otlpMysqlSqlSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }


}
