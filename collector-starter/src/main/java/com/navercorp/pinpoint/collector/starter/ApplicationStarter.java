package com.navercorp.pinpoint.collector.starter;

import com.navercorp.pinpoint.collector.env.CollectorEnvironmentApplicationListener;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Objects;

public class ApplicationStarter {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(CollectorApp.class);

    private final Class<?>[] sources;

    public ApplicationStarter(Class<?>... sources) {
        this.sources = Objects.requireNonNull(sources, "sources");
    }

    public void start(String[] args) {

        SpringApplicationBuilder builder = new SpringApplicationBuilder();

        builder.sources(sources);
        builder.web(WebApplicationType.SERVLET);
        builder.bannerMode(Banner.Mode.OFF);
        builder.listeners(new CollectorEnvironmentApplicationListener());

        SpringApplication springApplication = builder.build();
        springApplication.run(args);
    }

}
