package com.navercorp.pinpoint.collector.starter.multi.application;

import com.navercorp.pinpoint.common.server.banner.PinpointSpringBanner;
import com.navercorp.pinpoint.common.server.env.EnvironmentLoggingListener;
import com.navercorp.pinpoint.common.server.env.ExternalEnvironmentListener;
import com.navercorp.pinpoint.common.server.env.ProfileResolveListener;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import com.navercorp.pinpoint.metric.collector.MetricCollectorApp;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, TransactionAutoConfiguration.class})
public class MultiApplication {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(MultiApplication.class);

    public static final String EXTERNAL_PROPERTY_SOURCE_NAME = "CollectorExternalEnvironment";
    public static final String EXTERNAL_CONFIGURATION_KEY = "pinpoint.collector.config.location";

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder();
        builder.web(WebApplicationType.SERVLET);
        builder.bannerMode(Banner.Mode.OFF);

        builder.sources(MultiApplication.class);
        builder.listeners(new ProfileResolveListener());

        SpringApplicationBuilder collectorAppBuilder = createAppBuilder(builder, BasicCollectorApp.class, 1111);
        SpringApplicationBuilder metricAppBuilder = createAppBuilder(builder, MetricCollectorApp.class, 8081);

        collectorAppBuilder.build().run(args);
        metricAppBuilder.build().run(args);
    }

    private static SpringApplicationBuilder createAppBuilder(SpringApplicationBuilder builder, Class appClass, int port) {
        SpringApplicationBuilder collectorAppBuilder = builder.child(appClass)
                .web(WebApplicationType.SERVLET)
                .bannerMode(Banner.Mode.OFF)
                .listeners(new ProfileResolveListener())
                .listeners(new EnvironmentLoggingListener())
                .listeners(new ExternalEnvironmentListener(EXTERNAL_PROPERTY_SOURCE_NAME, EXTERNAL_CONFIGURATION_KEY))
                .listeners(new PinpointSpringBanner())
                .properties(String.format("server.port:%1s", port));

        return collectorAppBuilder;
    }
}
