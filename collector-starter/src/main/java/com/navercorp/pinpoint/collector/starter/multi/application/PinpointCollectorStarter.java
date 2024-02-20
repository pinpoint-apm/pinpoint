package com.navercorp.pinpoint.collector.starter.multi.application;

import com.navercorp.pinpoint.collector.starter.multi.application.type.CollectorType;
import com.navercorp.pinpoint.collector.starter.multi.application.type.CollectorTypeParser;
import com.navercorp.pinpoint.collector.starter.multi.application.type.CollectorTypeSet;
import com.navercorp.pinpoint.collector.starter.multi.application.type.FallbackCollectorTypeParser;
import com.navercorp.pinpoint.collector.starter.multi.application.type.ShellBlockerConfig;
import com.navercorp.pinpoint.collector.starter.multi.application.type.ShellCollectorTypeParser;
import com.navercorp.pinpoint.collector.starter.multi.application.type.SimpleCollectorTypeParser;
import com.navercorp.pinpoint.common.server.banner.PinpointSpringBanner;
import com.navercorp.pinpoint.common.server.env.AdditionalProfileListener;
import com.navercorp.pinpoint.common.server.env.EnvironmentLoggingListener;
import com.navercorp.pinpoint.common.server.env.ExternalEnvironmentListener;
import com.navercorp.pinpoint.common.server.env.ProfileResolveListener;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import com.navercorp.pinpoint.exceptiontrace.collector.ExceptionTraceCollectorConfig;
import com.navercorp.pinpoint.inspector.collector.InspectorCollectorConfig;
import com.navercorp.pinpoint.log.collector.LogCollectorModule;
import com.navercorp.pinpoint.metric.collector.MetricCollectorApp;
import com.navercorp.pinpoint.otlp.collector.OtlpMetricCollectorConfig;
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
import org.springframework.shell.boot.ApplicationRunnerAutoConfiguration;

import java.util.Arrays;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        TransactionAutoConfiguration.class,
        SpringDataWebAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class,
        RedisReactiveAutoConfiguration.class,
        ApplicationRunnerAutoConfiguration.class,
})
public class PinpointCollectorStarter {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(PinpointCollectorStarter.class);

    public static final String EXTERNAL_PROPERTY_SOURCE_NAME = "CollectorExternalEnvironment";
    public static final String EXTERNAL_CONFIGURATION_KEY = "pinpoint.collector.config.location";

    public static void main(String[] args) {
        logger.info("args:" + Arrays.toString(args));

        SpringApplicationBuilder builder = new SpringApplicationBuilder();

        builder.sources(PinpointCollectorStarter.class, ShellBlockerConfig.class);
        builder.listeners(new ProfileResolveListener());


        CollectorTypeParser parser = new FallbackCollectorTypeParser(
                new SimpleCollectorTypeParser(),
                new ShellCollectorTypeParser()
        );
        CollectorTypeSet types = parser.parse(args);
        logger.info(String.format("Collector type set: %s", types));

        if (types.hasType(CollectorType.BASIC)) {
            logger.info(String.format("Start %s collector", CollectorType.BASIC));
            SpringApplicationBuilder collectorAppBuilder = createAppBuilder(builder, 15400,
                    BasicCollectorApp.class,
                    UriStatCollectorConfig.class,
                    ExceptionTraceCollectorConfig.class,
                    InspectorCollectorConfig.class
            );
            collectorAppBuilder.listeners(new AdditionalProfileListener("metric"));
            collectorAppBuilder.listeners(new AdditionalProfileListener("uri"));
            collectorAppBuilder.build().run(args);
        }

        if (types.hasType(CollectorType.METRIC)) {
            logger.info(String.format("Start %s collector", CollectorType.METRIC));
            SpringApplicationBuilder metricAppBuilder = createAppBuilder(builder, 15200,
                    MetricCollectorApp.class,
                    OtlpMetricCollectorConfig.class);
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
        return builder.child(appClass)
                .web(WebApplicationType.SERVLET)
                .bannerMode(Banner.Mode.OFF)
                .listeners(new ProfileResolveListener())
                .listeners(new EnvironmentLoggingListener())
                .listeners(new ExternalEnvironmentListener(EXTERNAL_PROPERTY_SOURCE_NAME, EXTERNAL_CONFIGURATION_KEY))
                .listeners(new PinpointSpringBanner())
                .properties(String.format("server.port:%1s", port));
    }

}
