package com.navercorp.pinpoint.collector;


import com.navercorp.pinpoint.collector.config.ClusterModule;
import com.navercorp.pinpoint.collector.config.CollectorProperties;
import com.navercorp.pinpoint.collector.config.FlinkContextModule;
import com.navercorp.pinpoint.collector.config.MetricConfiguration;
import com.navercorp.pinpoint.collector.grpc.ssl.GrpcSslModule;
import com.navercorp.pinpoint.common.server.CommonsServerConfiguration;
import com.navercorp.pinpoint.common.server.config.TypeLoaderConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({
        "classpath:applicationContext-collector.xml",
        "classpath:servlet-context-collector.xml",

        "classpath:applicationContext-collector-grpc.xml",
        "classpath:applicationContext-collector-hbase.xml",
})
@Import({
        CollectorAppPropertySources.class,
        CommonsServerConfiguration.class,
        TypeLoaderConfiguration.class,

        FlinkContextModule.class,
        CollectorHbaseModule.class,

        ClusterModule.class,

        MetricConfiguration.class,

        GrpcSslModule.class
})
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.collector.handler",
        "com.navercorp.pinpoint.collector.manage",
        "com.navercorp.pinpoint.collector.mapper",
        "com.navercorp.pinpoint.collector.util",
        "com.navercorp.pinpoint.collector.service",
})
public class PinpointCollectorModule {

    @Bean
    public CollectorProperties collectorProperties() {
        return new CollectorProperties();
    }


}
