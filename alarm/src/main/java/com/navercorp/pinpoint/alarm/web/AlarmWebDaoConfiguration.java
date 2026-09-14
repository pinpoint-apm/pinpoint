package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.config.AlarmDaoConfigurationSupport;
import com.navercorp.pinpoint.mybatis.MyBatisConfigurationCustomizer;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

/** Every other alarm DAO bean is inherited; see {@link AlarmDaoConfigurationSupport}. */
@Configuration
public class AlarmWebDaoConfiguration extends AlarmDaoConfigurationSupport {

    /**
     * A compile-time constant so the pattern the test resolves is the one the bean uses:
     * a pattern that matches nothing builds an empty SqlSessionFactory instead of failing,
     * and the first DAO call dies far from the configuration that caused it.
     */
    static final String MAPPER_LOCATION = "classpath*:alarm/mapper/*Mapper.xml";

    @Bean
    public FactoryBean<SqlSessionFactory> alarmSqlSessionFactory(
            @Qualifier("myBatisConfigurationCustomizer") MyBatisConfigurationCustomizer customizer,
            @Qualifier("dataSource") DataSource dataSource,
            @Value(MAPPER_LOCATION) Resource[] mappers) {
        return newAlarmSqlSessionFactory(customizer, dataSource, mappers);
    }
}
