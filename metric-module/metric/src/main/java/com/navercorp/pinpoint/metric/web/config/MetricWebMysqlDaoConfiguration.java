package com.navercorp.pinpoint.metric.web.config;

import com.navercorp.pinpoint.metric.collector.config.MyBatisRegistryHandler;
import com.navercorp.pinpoint.pinot.mybatis.MyBatisConfiguration;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@org.springframework.context.annotation.Configuration
public class MetricWebMysqlDaoConfiguration {
    private final Logger logger = LogManager.getLogger(MetricWebMysqlDaoConfiguration.class);

    @Bean
    public SqlSessionFactoryBean metricSqlSessionFactory(
            @Qualifier("dataSource") DataSource dataSource,
            @Value("classpath*:/pinot-web/mapper/mysql/*Mapper.xml") Resource[] mappers) {

        for (Resource mapper : mappers) {
            logger.info("Mapper location: {}", mapper.getDescription());
        }

        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setMapperLocations(mappers);

        Configuration config = MyBatisConfiguration.defaultConfiguration();
        sessionFactoryBean.setConfiguration(config);

        MyBatisRegistryHandler registry = registryHandler();
        registry.registerTypeAlias(config.getTypeAliasRegistry());

        sessionFactoryBean.setFailFast(true);

        return sessionFactoryBean;
    }

    private MyBatisRegistryHandler registryHandler() {
        return new WebRegistryHandler();
    }

    @Bean
    public SqlSessionTemplate metricSqlSessionTemplate(
            @Qualifier("metricSqlSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }
}
