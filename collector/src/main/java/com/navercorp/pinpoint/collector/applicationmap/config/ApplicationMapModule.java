package com.navercorp.pinpoint.collector.applicationmap.config;

import com.navercorp.pinpoint.collector.applicationmap.dao.MapResponseTimeDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapStatisticsCalleeDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapStatisticsCallerDao;
import com.navercorp.pinpoint.collector.applicationmap.service.StatisticsService;
import com.navercorp.pinpoint.collector.applicationmap.service.StatisticsServiceImpl;
import com.navercorp.pinpoint.collector.applicationmap.statistics.config.BulkConfiguration;
import com.navercorp.pinpoint.collector.applicationmap.statistics.config.BulkFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.annotation.Validated;

@Configuration
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.collector.applicationmap.statistics.config",
        "com.navercorp.pinpoint.collector.applicationmap.dao.hbase",
})
@Import(value = {
        BulkFactory.class,

        BulkConfiguration.class,
        MapLinkConfiguration.class
})
public class ApplicationMapModule {
    private static final Logger logger = LogManager.getLogger(ApplicationMapModule.class);


    public ApplicationMapModule() {
        logger.info("Install {}", ApplicationMapModule.class.getName());
    }

    @Bean
    @Validated
    public StatisticsService statisticsService(MapStatisticsCalleeDao mapStatisticsCalleeDao,
                                               MapStatisticsCallerDao mapStatisticsCallerDao,
                                               MapResponseTimeDao mapResponseTimeDao) {
        return new StatisticsServiceImpl(mapStatisticsCalleeDao, mapStatisticsCallerDao, mapResponseTimeDao);
    }
}
