package com.navercorp.pinpoint.collector.starter.multi.application;

import com.navercorp.pinpoint.common.server.banner.PinpointSpringBanner;
import com.navercorp.pinpoint.common.server.env.AdditionalProfileListener;
import com.navercorp.pinpoint.common.server.env.EnvironmentLoggingListener;
import com.navercorp.pinpoint.common.server.env.ExternalEnvironmentListener;
import com.navercorp.pinpoint.common.server.env.ProfileResolveListener;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import com.navercorp.pinpoint.inspector.collector.InspectorCollectorApp;
import com.navercorp.pinpoint.log.collector.LogCollectorModule;
import com.navercorp.pinpoint.exceptiontrace.collector.ExceptionTraceCollectorConfig;
import com.navercorp.pinpoint.metric.collector.CollectorType;
import com.navercorp.pinpoint.metric.collector.CollectorTypeParser;
import com.navercorp.pinpoint.metric.collector.MetricCollectorApp;
import com.navercorp.pinpoint.metric.collector.TypeSet;
import com.navercorp.pinpoint.redis.RedisPropertySources;
import com.navercorp.pinpoint.uristat.collector.UriStatCollectorConfig;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Arrays;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        TransactionAutoConfiguration.class,
        SpringDataWebAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class,
        RedisReactiveAutoConfiguration.class
})
public class MultiApplication {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(MultiApplication.class);

    public static final String EXTERNAL_PROPERTY_SOURCE_NAME = "CollectorExternalEnvironment";
    public static final String EXTERNAL_CONFIGURATION_KEY = "pinpoint.collector.config.location";

    public static void main(String[] args) {
        logger.info("args:" + Arrays.toString(args));

        SpringApplicationBuilder builder = new SpringApplicationBuilder();

        builder.sources(MultiApplication.class);
        builder.listeners(new ProfileResolveListener());


        CollectorTypeParser parser = new CollectorTypeParser();
        TypeSet types = parser.parse(args);
        logger.info(String.format("MultiApplication --%s=%s", CollectorTypeParser.COLLECTOR_TYPE_KEY, types));

        if (types.hasType(CollectorType.BASIC)) {
            logger.info(String.format("Start %s collector", CollectorType.BASIC));
            SpringApplicationBuilder collectorAppBuilder = createAppBuilder(builder, 15400,
                    BasicCollectorApp.class,
                    UriStatCollectorConfig.class,
                    ExceptionTraceCollectorConfig.class
            );
            collectorAppBuilder.listeners(new AdditionalProfileListener("metric"));
            collectorAppBuilder.listeners(new AdditionalProfileListener("uri"));
            collectorAppBuilder.build().run(args);
        }

        if (types.hasType(CollectorType.BASIC_WITH_INSPECTOR)) {
            logger.info(String.format("Start %s collector", CollectorType.BASIC_WITH_INSPECTOR));
            SpringApplicationBuilder collectorAppBuilder = createAppBuilder(builder, 15400,
                    BasicCollectorApp.class,
                    UriStatCollectorConfig.class,
                    ExceptionTraceCollectorConfig.class,
                    InspectorCollectorApp.class
            );
            collectorAppBuilder.build().run(args);
        }

        if (types.hasType(CollectorType.METRIC)) {
            logger.info(String.format("Start %s collector", CollectorType.METRIC));
            SpringApplicationBuilder metricAppBuilder = createAppBuilder(builder, 15200, MetricCollectorApp.class);
            metricAppBuilder.listeners(new AdditionalProfileListener("metric"));
            metricAppBuilder.build().run(args);
        }

        if (types.hasType(CollectorType.LOG)) {
            logger.info(String.format("Start %s collector", CollectorType.LOG));
            SpringApplicationBuilder logAppBuilder = createAppBuilder(builder, 0,
                    LogCollectorModule.class,
                    RedisPropertySources.class
            )
                    .web(WebApplicationType.NONE);
            logAppBuilder.build().run(args);
        }
    }


    private static SpringApplicationBuilder createAppBuilder(SpringApplicationBuilder builder, int port, Class<?>... appClass) {
        SpringApplicationBuilder appBuilder = builder.child(appClass)
                .web(WebApplicationType.SERVLET)
                .bannerMode(Banner.Mode.OFF)
                .listeners(new ProfileResolveListener())
                .listeners(new EnvironmentLoggingListener())
                .listeners(new ExternalEnvironmentListener(EXTERNAL_PROPERTY_SOURCE_NAME, EXTERNAL_CONFIGURATION_KEY))
                .listeners(new PinpointSpringBanner())
                .properties(String.format("server.port:%1s", port));

        return appBuilder;
    }
}
