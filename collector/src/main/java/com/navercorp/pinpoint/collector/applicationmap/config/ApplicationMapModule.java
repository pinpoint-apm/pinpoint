package com.navercorp.pinpoint.collector.applicationmap.config;

import com.navercorp.pinpoint.collector.applicationmap.dao.MapInLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapResponseTimeDao;
import com.navercorp.pinpoint.collector.applicationmap.service.LinkService;
import com.navercorp.pinpoint.collector.applicationmap.service.LinkServiceImpl;
import com.navercorp.pinpoint.collector.applicationmap.statistics.config.BulkConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.annotation.Validated;

@Configuration
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.collector.applicationmap.dao.hbase",
})
@Import(value = {
        BulkConfiguration.class,

        MapLinkProperties.class
})
public class ApplicationMapModule {
    private static final Logger logger = LogManager.getLogger(ApplicationMapModule.class);


    public ApplicationMapModule() {
        logger.info("Install {}", ApplicationMapModule.class.getName());
    }

    @Bean
    @Validated
    public LinkService statisticsService(MapInLinkDao inLinkDao,
                                         MapOutLinkDao outLinkDao,
                                         MapResponseTimeDao responseTimeDao) {
        return new LinkServiceImpl(inLinkDao, outLinkDao, responseTimeDao);
    }
}
