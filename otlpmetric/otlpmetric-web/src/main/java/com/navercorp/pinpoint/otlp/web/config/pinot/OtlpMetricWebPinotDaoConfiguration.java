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

package com.navercorp.pinpoint.otlp.web.config.pinot;

import com.navercorp.pinpoint.mybatis.MyBatisConfigurationCustomizer;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import com.navercorp.pinpoint.pinot.mybatis.PinotAsyncTemplate;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@Configuration
public class OtlpMetricWebPinotDaoConfiguration {

    @Bean
    public FactoryBean<SqlSessionFactory> otlpMetricPinotSessionFactory(
            @Qualifier("pinotConfigurationCustomizer") MyBatisConfigurationCustomizer customizer,
            @Qualifier("pinotDataSource") DataSource dataSource,
            @Value("classpath:otlp/web/mapper/pinot/*Mapper.xml") Resource[] mappers) {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();

        sessionFactoryBean.setDataSource(dataSource);

        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        customizer.customize(config);
        registryHandler(config);
        sessionFactoryBean.setConfiguration(config);

        sessionFactoryBean.setMapperLocations(mappers);
        sessionFactoryBean.setFailFast(true);
        sessionFactoryBean.setTransactionFactory(transactionFactory());


        return sessionFactoryBean;
    }

    private TransactionFactory transactionFactory() {
        return new ManagedTransactionFactory();
    }

    private void registryHandler(org.apache.ibatis.session.Configuration config) {
        MyBatisRegistryHandler registryHandler = new OtlpMetricPinotRegistryHandler();
        registryHandler.registerTypeAlias(config.getTypeAliasRegistry());
        registryHandler.registerTypeHandler(config.getTypeHandlerRegistry());
    }

    @Bean
    public SqlSessionTemplate otlpMetricPinotSessionTemplate(
            @Qualifier("otlpMetricPinotSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }

    @Bean
    public PinotAsyncTemplate otlpMetricPinotAsyncTemplate(
            @Qualifier("otlpMetricPinotSessionFactory") SqlSessionFactory sessionFactory) {
        return new PinotAsyncTemplate(sessionFactory);
    }

}
