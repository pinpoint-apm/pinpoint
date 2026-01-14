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
 *
 */
package com.navercorp.pinpoint.featureflag.config;

import com.navercorp.pinpoint.datasource.MainDataSourceConfiguration;
import com.navercorp.pinpoint.featureflag.dao.vo.FeatureFlagParameter;
import com.navercorp.pinpoint.featureflag.dao.vo.FeatureFlagResult;
import com.navercorp.pinpoint.mybatis.MyBatisConfiguration;
import com.navercorp.pinpoint.mybatis.MyBatisConfigurationCustomizer;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import com.navercorp.pinpoint.mybatis.plugin.BindingLogPlugin;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = {"com.navercorp.pinpoint.featureflag.service.database", "com.navercorp.pinpoint.featureflag.dao"})
@Import({
        MainDataSourceConfiguration.class,
        MyBatisConfiguration.class
})
@EnableScheduling
public class DatabaseFeatureFlagConfiguration {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Bean
    MyBatisRegistryHandler featureFlagMyBatisRegistryHandler() {
        return new MyBatisRegistryHandler() {
            @Override
            public void registerTypeAlias(TypeAliasRegistry typeAliasRegistry) {
                typeAliasRegistry.registerAlias(FeatureFlagParameter.class);
                typeAliasRegistry.registerAlias(FeatureFlagResult.class);
            }

            @Override
            public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {

            }
        };
    }

    @Bean
    public FactoryBean<SqlSessionFactory> featureFlagSqlSessionFactory(
            @Qualifier("myBatisConfigurationCustomizer") MyBatisConfigurationCustomizer customizer,
            @Qualifier("dataSource") DataSource dataSource,
            @Qualifier("featureFlagMyBatisRegistryHandler") MyBatisRegistryHandler myBatisRegistryHandler,
            @Value("classpath:featureflag/mapper/*Mapper.xml") Resource[] mappers,
            BindingLogPlugin bindingLogPlugin) {

        for (Resource mapper : mappers) {
            logger.info("Mapper location: {}", mapper.getDescription());
        }

        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setMapperLocations(mappers);

        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        customizer.customize(config);

        sessionFactoryBean.setConfiguration(config);
        sessionFactoryBean.setFailFast(true);
        sessionFactoryBean.setPlugins(bindingLogPlugin);

        myBatisRegistryHandler.registerTypeAlias(config.getTypeAliasRegistry());
        myBatisRegistryHandler.registerTypeHandler(config.getTypeHandlerRegistry());

        return sessionFactoryBean;
    }

    @Bean
    public SqlSessionTemplate featureFlagSqlSessionTemplate(
            @Qualifier("featureFlagSqlSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }
}
