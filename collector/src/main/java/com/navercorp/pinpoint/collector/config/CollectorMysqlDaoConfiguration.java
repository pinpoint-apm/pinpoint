/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.mybatis.MyBatisConfiguration;
import com.navercorp.pinpoint.mybatis.MyBatisConfigurationCustomizer;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandlerChain;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.util.List;

@org.springframework.context.annotation.Configuration
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.collector.dao.mysql",
})
@Import(MyBatisConfiguration.class)
public class CollectorMysqlDaoConfiguration {

    private final Logger logger = LogManager.getLogger(CollectorMysqlDaoConfiguration.class);


    @Bean
    public CollectorMyBatisRegistryHandler webCommonMyBatisRegistryHandler() {
        return new CollectorCommonMyBatisRegistryHandler();
    }

    @Bean
    public MyBatisRegistryHandler webMyBatisRegistryHandler(List<CollectorMyBatisRegistryHandler> handlers) {
        return new MyBatisRegistryHandlerChain(handlers);
    }

    @Bean
    public FactoryBean<SqlSessionFactory> sqlSessionFactory(
            @Qualifier("myBatisConfigurationCustomizer") MyBatisConfigurationCustomizer customizer,
            @Qualifier("dataSource") DataSource dataSource,
//            @Value("classpath:/mybatis-config.xml") Resource mybatisConfig,
            @Qualifier("webMyBatisRegistryHandler") MyBatisRegistryHandler myBatisRegistryHandler,
            @Value("classpath*:mapper/*Mapper.xml") Resource[] mappers,
            BindingLogPlugin bindingLogPlugin) {

        for (Resource mapper : mappers) {
            logger.info("Mapper location: {}", mapper.getDescription());
        }

        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setMapperLocations(mappers);

        Configuration config = new Configuration();
        customizer.customize(config);

        sessionFactoryBean.setConfiguration(config);
        sessionFactoryBean.setFailFast(true);
        sessionFactoryBean.setPlugins(bindingLogPlugin);

        myBatisRegistryHandler.registerTypeAlias(config.getTypeAliasRegistry());
        myBatisRegistryHandler.registerTypeHandler(config.getTypeHandlerRegistry());

        return sessionFactoryBean;
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(
            @Qualifier("sqlSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }

}
