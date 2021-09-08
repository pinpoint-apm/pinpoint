package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import com.navercorp.pinpoint.web.env.EnvironmentLoggingListener;
import com.navercorp.pinpoint.web.env.ExternalEnvironmentApplicationListener;
import com.navercorp.pinpoint.web.env.ProfileResolveApplicationListener;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Objects;

public class WebStarter {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(WebApp.class);

    private final Class<?>[] sources;

    public WebStarter(Class<?>... sources) {
        this.sources = Objects.requireNonNull(sources, "sources");
    }


    public void start(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder();
        builder.sources(sources);
        builder.web(WebApplicationType.SERVLET);
        builder.bannerMode(Banner.Mode.OFF);

        builder.listeners(new ProfileResolveApplicationListener());
        builder.listeners(new ExternalEnvironmentApplicationListener());
        builder.listeners(new EnvironmentLoggingListener());

        SpringApplication springApplication = builder.build();
        springApplication.run(args);

    }

}
