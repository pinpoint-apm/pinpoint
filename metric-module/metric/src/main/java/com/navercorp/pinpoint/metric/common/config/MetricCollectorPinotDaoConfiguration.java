package com.navercorp.pinpoint.metric.common.config;

import com.navercorp.pinpoint.metric.collector.config.MyBatisRegistryHandler;
import com.navercorp.pinpoint.pinot.mybatis.MyBatisConfiguration;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;

/**
 * @author Woonduk Kang(emeroad)
 */
@org.springframework.context.annotation.Configuration
public class MetricCollectorPinotDaoConfiguration {
    private final Logger logger = LogManager.getLogger(MetricCollectorPinotDaoConfiguration.class);

    @Bean
    public TransactionManager pinotTransactionManager(@Qualifier("pinotDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }


    @Bean
    public FactoryBean<SqlSessionFactory> sqlPinotSessionFactory(
            @Qualifier("pinotDataSource") DataSource dataSource,
            @Value("classpath*:/pinot-collector/mapper/pinot/*Mapper.xml") Resource[] mappers) {

        for (Resource mapper : mappers) {
            logger.info("Mapper location: {}", mapper.getDescription());
        }

        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setMapperLocations(mappers);
        sessionFactoryBean.setTransactionFactory(transactionFactory());

        Configuration config = MyBatisConfiguration.defaultConfiguration();
        sessionFactoryBean.setConfiguration(config);

        MyBatisRegistryHandler registry = registryHandler();
        registry.registerTypeAlias(config.getTypeAliasRegistry());
        registry.registerTypeHandler(config.getTypeHandlerRegistry());

        return sessionFactoryBean;
    }

    private TransactionFactory transactionFactory() {
        return new ManagedTransactionFactory();
    }

    private MyBatisRegistryHandler registryHandler() {
        return new CommonRegistryHandler();
    }


    @Bean
    public SqlSessionTemplate sqlPinotSessionTemplate(
            @Qualifier("sqlPinotSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }
}
