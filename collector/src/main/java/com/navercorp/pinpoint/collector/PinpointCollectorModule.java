package com.navercorp.pinpoint.collector;


import com.navercorp.pinpoint.collector.config.ClusterModule;
import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import com.navercorp.pinpoint.collector.config.CollectorMysqlDaoConfiguration;
import com.navercorp.pinpoint.collector.config.CollectorProperties;
import com.navercorp.pinpoint.collector.config.FlinkContextModule;
import com.navercorp.pinpoint.collector.config.MetricConfiguration;
import com.navercorp.pinpoint.collector.grpc.config.CollectorGrpcConfiguration;
import com.navercorp.pinpoint.collector.grpc.ssl.GrpcSslModule;
import com.navercorp.pinpoint.common.server.CommonsServerConfiguration;
import com.navercorp.pinpoint.common.server.config.TypeLoaderConfiguration;
import com.navercorp.pinpoint.datasource.MainDataSourceConfiguration;
import com.navercorp.pinpoint.datasource.MainDataSourcePropertySource;
import com.navercorp.pinpoint.realtime.collector.RealtimeCollectorModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({
        "classpath:applicationContext-collector.xml",
        "classpath:servlet-context-collector.xml",


})
@Import({
        CollectorAppPropertySources.class,
        CommonsServerConfiguration.class,
        TypeLoaderConfiguration.class,

        FlinkContextModule.class,
        CollectorConfiguration.class,
        CollectorHbaseModule.class,
        CollectorMysqlDaoConfiguration.class,
        MainDataSourcePropertySource.class,
        MainDataSourceConfiguration.class,

        CollectorGrpcConfiguration.class,

        ClusterModule.class,

        MetricConfiguration.class,

        GrpcSslModule.class,

        RealtimeCollectorModule.class,
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
