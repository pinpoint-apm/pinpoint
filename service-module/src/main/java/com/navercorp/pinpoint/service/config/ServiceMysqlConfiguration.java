package com.navercorp.pinpoint.service.config;

import com.navercorp.pinpoint.mybatis.MyBatisConfiguration;
import com.navercorp.pinpoint.mybatis.MyBatisConfigurationCustomizer;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandlerChain;
import com.navercorp.pinpoint.mybatis.plugin.BindingLogPlugin;
import com.navercorp.pinpoint.service.dao.ServiceRegistryDao;
import com.navercorp.pinpoint.service.dao.mysql.MysqlServiceRegistryDao;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.retry.annotation.EnableRetry;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableRetry
@Import({
        MyBatisConfiguration.class,
})
public class ServiceMysqlConfiguration {

    private final Logger logger = LogManager.getLogger(ServiceMysqlConfiguration.class);

    @Bean
    public ServiceMyBatisRegistryHandler serviceCommonMyBatisRegistryHandler() {
        return new ServiceCommonMyBatisRegistryHandler();
    }

    @Bean
    public MyBatisRegistryHandler serviceMyBatisHandler(List<ServiceMyBatisRegistryHandler> handlers) {
        return new MyBatisRegistryHandlerChain(handlers);
    }

    @Bean
    public FactoryBean<SqlSessionFactory> serviceMysqlSqlSessionFactory(
            @Qualifier("myBatisConfigurationCustomizer") MyBatisConfigurationCustomizer customizer,
            @Qualifier("dataSource") DataSource dataSource,
            @Qualifier("serviceMyBatisHandler") MyBatisRegistryHandler registryHandler,
            @Value("classpath*:/service/web/mapper/*.xml") Resource[] mappers,
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

        registryHandler.registerTypeAlias(config.getTypeAliasRegistry());
        registryHandler.registerTypeHandler(config.getTypeHandlerRegistry());

        return sessionFactoryBean;
    }

    @Bean
    public SqlSessionTemplate serviceMysqlSqlSessionTemplate(
            @Qualifier("serviceMysqlSqlSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }

    @Bean
    public ServiceRegistryDao serviceRegistryDao(
            @Qualifier("serviceMysqlSqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        return new MysqlServiceRegistryDao(sqlSessionTemplate);
    }
}
