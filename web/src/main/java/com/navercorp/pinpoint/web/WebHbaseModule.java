package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.common.hbase.config.DistributorConfiguration;
import com.navercorp.pinpoint.common.hbase.config.HbaseNamespaceConfiguration;
import com.navercorp.pinpoint.common.server.CommonsHbaseConfiguration;
import com.navercorp.pinpoint.web.dao.hbase.config.AgentStatDaoConfiguration;
import com.navercorp.pinpoint.web.dao.hbase.config.AgentStatOperationConfiguration;
import com.navercorp.pinpoint.web.dao.hbase.config.ApplicationMetricConfiguration;
import com.navercorp.pinpoint.web.dao.hbase.config.SampledAgentStatDaoConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import({
        CommonsHbaseConfiguration.class,
        AgentStatDaoConfiguration.class,
        AgentStatOperationConfiguration.class,

        ApplicationMetricConfiguration.class,
        SampledAgentStatDaoConfiguration.class,


        HbaseNamespaceConfiguration.class,
        DistributorConfiguration.class,
})
@ImportResource({
        "classpath:applicationContext-web-hbase.xml"
})
@ComponentScan(
        basePackages = {
                "com.navercorp.pinpoint.web.dao.hbase"
        },
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASPECTJ,
                        pattern = "com.navercorp.pinpoint.web.dao.hbase.config.*"
                )
        }
)
@PropertySource(name = "WebHbaseModule", value = {
        "classpath:hbase-root.properties",
        "classpath:profiles/${pinpoint.profiles.active:release}/hbase.properties"
})
public class WebHbaseModule {
    private final Logger logger = LogManager.getLogger(WebHbaseModule.class);

    public WebHbaseModule() {
        logger.info("Install {}", WebHbaseModule.class.getSimpleName());
    }
}
