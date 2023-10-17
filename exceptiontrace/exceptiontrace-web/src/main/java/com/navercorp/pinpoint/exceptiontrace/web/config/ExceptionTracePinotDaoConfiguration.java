/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.exceptiontrace.web.config;

import com.navercorp.pinpoint.mybatis.MyBatisConfigurationCustomizer;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

/**
 * @author intr3p1d
 */
public class ExceptionTracePinotDaoConfiguration {

    @Bean
    public SqlSessionFactory exceptionTracePinotSessionFactory(
            @Qualifier("pinotConfigurationCustomizer") MyBatisConfigurationCustomizer customizer,
            @Qualifier("pinotDataSource") DataSource dataSource,
            @Value("classpath:exceptiontrace/mapper/*Mapper.xml") Resource[] mappers
    ) throws Exception {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();

        sessionFactoryBean.setDataSource(dataSource);

        Configuration config = new Configuration();
        customizer.customize(config);
        registryHandler(config);

        sessionFactoryBean.setConfiguration(config);
        sessionFactoryBean.setMapperLocations(mappers);
        sessionFactoryBean.setFailFast(true);
        sessionFactoryBean.setTransactionFactory(transactionFactory());

        return sessionFactoryBean.getObject();
    }

    private ManagedTransactionFactory transactionFactory() {
        return new ManagedTransactionFactory();
    }

    private void registryHandler(Configuration config) {
        MyBatisRegistryHandler registryHandler = new ExceptionTraceRegistryHandler();
        registryHandler.registerTypeAlias(config.getTypeAliasRegistry());
        registryHandler.registerTypeHandler(config.getTypeHandlerRegistry());
    }


    @Bean
    public SqlSessionTemplate exceptionTracePinotSessionTemplate(
            @Qualifier("exceptionTracePinotSessionFactory") SqlSessionFactory sessionFactory
    ) {
        return new SqlSessionTemplate(sessionFactory);
    }
}
