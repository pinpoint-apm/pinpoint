package com.navercorp.pinpoint.otlp.web.config;

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
            @Value("classpath:mapper/otlpmetric/*Mapper.xml") Resource[] mappers) {
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
        MyBatisRegistryHandler registryHandler = new OtlpMetricWebRegistryHandler();
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
