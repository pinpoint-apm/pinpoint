package com.navercorp.pinpoint.metric.web.config;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.navercorp.pinpoint.metric.web.mapping.Mappings;
import com.navercorp.pinpoint.metric.web.service.YMLSystemMetricBasicGroupManager;
import com.navercorp.pinpoint.mybatis.MyBatisConfiguration;
import com.navercorp.pinpoint.mybatis.MyBatisConfigurationCustomizer;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import com.navercorp.pinpoint.pinot.mybatis.PinotAsyncTemplate;
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
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Woonduk Kang(emeroad)
 */
@org.springframework.context.annotation.Configuration
@Import(MyBatisConfiguration.class)
public class MetricWebPinotDaoConfiguration {
    private final Logger logger = LogManager.getLogger(MetricWebPinotDaoConfiguration.class);

    @Bean
    public TransactionManager pinotTransactionManager(@Qualifier("pinotDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }


    @Bean
    public FactoryBean<SqlSessionFactory> sqlPinotSessionFactory(
            @Qualifier("pinotConfigurationCustomizer") MyBatisConfigurationCustomizer customizer,
            @Qualifier("pinotDataSource") DataSource dataSource,
            @Value("classpath*:/pinot-web/mapper/pinot/*Mapper.xml") Resource[] mappers) {

        for (Resource mapper : mappers) {
            logger.info("Mapper location: {}", mapper.getDescription());
        }

        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setMapperLocations(mappers);
        sessionFactoryBean.setTransactionFactory(transactionFactory());

        Configuration config = new Configuration();
        customizer.customize(config);

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
        return new WebRegistryHandler();
    }


    @Bean
    public SqlSessionTemplate sqlPinotSessionTemplate(
            @Qualifier("sqlPinotSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }

    @Bean
    public PinotAsyncTemplate pinotAsyncTemplate(
            @Qualifier("sqlPinotSessionFactory") SqlSessionFactory sessionFactory) {
        return new PinotAsyncTemplate(sessionFactory);
    }

    @Bean
    public Mappings telegrafMetricDefinition(@Value(YMLSystemMetricBasicGroupManager.TELEGRAF_METRIC)
                                            Resource telegrafMetric,
                                            YAMLMapper mapper) throws IOException {

        InputStream stream = telegrafMetric.getInputStream();

        return mapper.readValue(stream, Mappings.class);
    }
}
