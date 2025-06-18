package com.navercorp.pinpoint.service;

import com.navercorp.pinpoint.service.component.StaticServiceRegistry;
import com.navercorp.pinpoint.service.config.mysql.ServiceMysqlDaoConfiguration;
import com.navercorp.pinpoint.service.dao.EmptyServiceDao;
import com.navercorp.pinpoint.service.dao.ServiceDao;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@Import({
        ServiceMysqlDaoConfiguration.class,
})
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.service.dao",
        "com.navercorp.pinpoint.service.service",
})
public class ServiceModule {

    @Bean
    public StaticServiceRegistry staticServiceRegistry() {
        return new StaticServiceRegistry();
    }

    @Bean
    @ConditionalOnMissingBean(ServiceDao.class)
    public ServiceDao serviceDao() {
        return new EmptyServiceDao();
    }
}
