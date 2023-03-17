package com.navercorp.pinpoint.uristat.web.config;

import com.navercorp.pinpoint.pinot.mybatis.MyBatisConfiguration;
import com.navercorp.pinpoint.uristat.web.model.UriStatHistogram;
import com.navercorp.pinpoint.uristat.web.model.UriStatSummary;
import com.navercorp.pinpoint.uristat.web.util.UriStatQueryParameter;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

/**
 * @author Woonduk Kang(emeroad)
 */
@Configuration
public class UriStatPinotDaoConfiguration {

    @Bean
    public SqlSessionFactory uriStatPinotSessionFactory(
            @Qualifier("pinotDataSource") DataSource dataSource,
            @Value("classpath:mapper/uristat/*Mapper.xml") Resource[] mappers) throws Exception {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();

        sessionFactoryBean.setDataSource(dataSource);

        sessionFactoryBean.setConfiguration(newConfiguration());

        sessionFactoryBean.setMapperLocations(mappers);
        sessionFactoryBean.setFailFast(true);
        sessionFactoryBean.setTransactionFactory(new ManagedTransactionFactory());


        return sessionFactoryBean.getObject();
    }

    private org.apache.ibatis.session.Configuration newConfiguration() {
        org.apache.ibatis.session.Configuration config = MyBatisConfiguration.defaultConfiguration();

        TypeAliasRegistry typeAliasRegistry = config.getTypeAliasRegistry();
        typeAliasRegistry.registerAlias("UriStatHistogram", UriStatHistogram.class);
        typeAliasRegistry.registerAlias("UriStatSummary", UriStatSummary.class);
        typeAliasRegistry.registerAlias("UriStatQueryParameter", UriStatQueryParameter.class);
        return config;
    }

    @Bean
    public SqlSessionTemplate uriStatPinotSessionTemplate(
            @Qualifier("uriStatPinotSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }

}
