package com.navercorp.pinpoint.collector.starter.multi.application;

import com.navercorp.pinpoint.collector.env.CollectorEnvironmentApplicationListener;
import com.navercorp.pinpoint.common.server.profile.ProfileApplicationListener;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import com.navercorp.pinpoint.metric.collector.MetricCollectorApp;
import com.navercorp.pinpoint.metric.collector.env.MetricEnvironmentApplicationListener;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class MultiApplication {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(MultiApplication.class);

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder();
        builder.web(WebApplicationType.SERVLET);
        builder.bannerMode(Banner.Mode.OFF);

        builder.sources(MultiApplication.class);

        SpringApplicationBuilder collectorAppBuilder = builder.child(BasicCollectorApp.class)
                .web(WebApplicationType.SERVLET)
                .bannerMode(Banner.Mode.OFF)
                .listeners(new CollectorEnvironmentApplicationListener())
                .properties(new StringBuilder("server.port:").append(1111).toString())
                .listeners(new ProfileApplicationListener());

        SpringApplicationBuilder metricAppBuilder = builder.child(MetricCollectorApp.class)
                .web(WebApplicationType.SERVLET)
                .bannerMode(Banner.Mode.OFF)
                .listeners(new MetricEnvironmentApplicationListener())
                .properties(new StringBuilder("server.port:").append(8081).toString());

        collectorAppBuilder.build().run(args);
        metricAppBuilder.build().run(args);
    }
}
