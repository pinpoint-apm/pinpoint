package com.navercorp.pinpoint.collector;

import com.navercorp.pinpoint.common.server.env.EnvironmentLoggingListener;
import com.navercorp.pinpoint.common.server.env.ExternalEnvironmentListener;
import com.navercorp.pinpoint.common.server.env.ProfileResolveListener;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Objects;

public class CollectorStarter {

    public static final String EXTERNAL_PROPERTY_SOURCE_NAME = "CollectorExternalEnvironment";
    public static final String EXTERNAL_CONFIGURATION_KEY = "pinpoint.collector.config.location";

    private static final ServerBootLogger logger = ServerBootLogger.getLogger(CollectorApp.class);

    private final Class<?>[] sources;

    public CollectorStarter(Class<?>... sources) {
        this.sources = Objects.requireNonNull(sources, "sources");
    }

    public void start(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder();

        builder.sources(sources);
        builder.web(WebApplicationType.SERVLET);
        builder.bannerMode(Banner.Mode.OFF);

        builder.listeners(new ProfileResolveListener());
        builder.listeners(new EnvironmentLoggingListener());
        builder.listeners(new ExternalEnvironmentListener(EXTERNAL_PROPERTY_SOURCE_NAME, EXTERNAL_CONFIGURATION_KEY));

        SpringApplication springApplication = builder.build();
        springApplication.run(args);
    }

}
