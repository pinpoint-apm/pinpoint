package com.navercorp.pinpoint.collector;


import com.navercorp.pinpoint.collector.config.ClusterModule;
import com.navercorp.pinpoint.collector.config.CollectorCommonConfiguration;
import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import com.navercorp.pinpoint.collector.config.CollectorPinpointIdCacheConfiguration;
import com.navercorp.pinpoint.collector.grpc.CollectorGrpcConfiguration;
import com.navercorp.pinpoint.collector.grpc.ssl.GrpcSslModule;
import com.navercorp.pinpoint.collector.manage.CollectorAdminConfiguration;
import com.navercorp.pinpoint.common.server.CommonsServerConfiguration;
import com.navercorp.pinpoint.common.server.config.TypeLoaderConfiguration;
import com.navercorp.pinpoint.realtime.collector.RealtimeCollectorModule;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        CollectorAppPropertySources.class,
        CommonsServerConfiguration.class,
        CollectorAdminConfiguration.class,
        CollectorMvcConfig.class,

        CollectorCommonConfiguration.class,

        TypeLoaderConfiguration.class,

        CollectorConfiguration.class,
        CollectorHbaseModule.class,

        CollectorGrpcConfiguration.class,

        ClusterModule.class,

        GrpcSslModule.class,

        RealtimeCollectorModule.class,

        CollectorPinpointIdCacheConfiguration.class,
})
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.collector.handler",
        "com.navercorp.pinpoint.collector.manage",
        "com.navercorp.pinpoint.collector.mapper",
        "com.navercorp.pinpoint.collector.util",
        "com.navercorp.pinpoint.collector.service",
        "com.navercorp.pinpoint.collector.controller",
})
public class PinpointCollectorModule {


}
