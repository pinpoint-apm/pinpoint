package com.navercorp.pinpoint.uristat.web.config;

import com.navercorp.pinpoint.metric.collector.config.MyBatisRegistryHandler;
import com.navercorp.pinpoint.pinot.mybatis.MyBatisConfiguration;
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
            @Qualifier("pinotDataSource") DataSource dataSource,
            @Value("classpath:mapper/uristat/*Mapper.xml") Resource[] mappers) {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();

        sessionFactoryBean.setDataSource(dataSource);

        sessionFactoryBean.setConfiguration(newConfiguration());

        sessionFactoryBean.setMapperLocations(mappers);
        sessionFactoryBean.setFailFast(true);
        sessionFactoryBean.setTransactionFactory(transactionFactory());


        return sessionFactoryBean;
    }

    private TransactionFactory transactionFactory() {
        return new ManagedTransactionFactory();
    }

    private Configuration newConfiguration() {
        Configuration config = MyBatisConfiguration.defaultConfiguration();

        MyBatisRegistryHandler registryHandler = registryHandler();
        registryHandler.registerTypeAlias(config.getTypeAliasRegistry());
        registryHandler.registerTypeHandler(config.getTypeHandlerRegistry());
        return config;
    }

    private MyBatisRegistryHandler registryHandler() {
        return new UriRegistryHandler();
    }

    @Bean
    public SqlSessionTemplate uriStatPinotSessionTemplate(
            @Qualifier("uriStatPinotSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }

}
