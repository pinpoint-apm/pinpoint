package com.navercorp.pinpoint.metric.collector.config;

import com.navercorp.pinpoint.metric.common.config.MetricSqlSessionFactoryBuilder;
import com.navercorp.pinpoint.pinot.mybatis.MyBatisConfiguration;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionTemplate;
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
    public SqlSessionFactory sqlPinotSessionFactory(
            @Qualifier("pinotDataSource") DataSource dataSource,
            @Value("classpath*:/pinot-collector/mapper/pinot/*Mapper.xml") Resource[] mappers) throws Exception {

        for (Resource mapper : mappers) {
            logger.info("Mapper location: {}", mapper.getDescription());
        }

        MetricSqlSessionFactoryBuilder factory = new MetricSqlSessionFactoryBuilder();
        factory.setDataSource(dataSource);
        factory.setMappers(mappers);

        Configuration config = MyBatisConfiguration.defaultConfiguration();
        factory.setConfiguration(config);

        factory.registerCommonTypeAlias();
        factory.registerCommonTypeHandler();
        factory.setTransactionFactory(new ManagedTransactionFactory());
        return factory.build();
    }


    @Bean
    public SqlSessionTemplate sqlPinotSessionTemplate(
            @Qualifier("sqlPinotSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }
}
