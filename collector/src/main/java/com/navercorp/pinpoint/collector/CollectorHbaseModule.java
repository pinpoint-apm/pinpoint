package com.navercorp.pinpoint.collector;

import com.navercorp.pinpoint.collector.config.BatchHbaseClientConfiguration;
import com.navercorp.pinpoint.common.hbase.config.DistributorConfiguration;
import com.navercorp.pinpoint.common.hbase.config.HbaseClientConfiguration;
import com.navercorp.pinpoint.common.hbase.config.HbaseMultiplexerProperties;
import com.navercorp.pinpoint.common.hbase.config.HbaseNamespaceConfiguration;
import com.navercorp.pinpoint.common.server.CommonsHbaseConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ImportResource({
        "classpath:applicationContext-collector-hbase.xml",
})
@Import({
        CommonsHbaseConfiguration.class,
        HbaseNamespaceConfiguration.class,
        DistributorConfiguration.class,

        HbaseClientConfiguration.class,
        BatchHbaseClientConfiguration.class,
})
@ComponentScan({
        "com.navercorp.pinpoint.collector.dao.hbase"
})
@PropertySource(name = "CollectorHbaseModule", value = {
        "classpath:hbase-root.properties",
        "classpath:profiles/${pinpoint.profiles.active:local}/hbase.properties"
})
public class CollectorHbaseModule {

    @Bean
    @ConfigurationProperties(prefix = "hbase.client.async")
    public HbaseMultiplexerProperties hbaseMultiplexerProperties() {
        return new HbaseMultiplexerProperties();
    }
}
