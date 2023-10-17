package com.navercorp.pinpoint.uristat.web.config;

import com.navercorp.pinpoint.mybatis.MyBatisConfigurationCustomizer;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

/**
 * @author Woonduk Kang(emeroad)
 */
@org.springframework.context.annotation.Configuration
public class UriStatPinotDaoConfiguration {

    @Bean
    public FactoryBean<SqlSessionFactory> uriStatPinotSessionFactory(
            @Qualifier("pinotConfigurationCustomizer") MyBatisConfigurationCustomizer customizer,
            @Qualifier("pinotDataSource") DataSource dataSource,
            @Value("classpath:mapper/uristat/*Mapper.xml") Resource[] mappers) {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();

        sessionFactoryBean.setDataSource(dataSource);

        Configuration config = new Configuration();
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

    private void registryHandler(Configuration config) {
        MyBatisRegistryHandler registryHandler = new UriRegistryHandler();
        registryHandler.registerTypeAlias(config.getTypeAliasRegistry());
        registryHandler.registerTypeHandler(config.getTypeHandlerRegistry());
    }

    @Bean
    public SqlSessionTemplate uriStatPinotSessionTemplate(
            @Qualifier("uriStatPinotSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }

}
