package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.common.server.profile.ProfileApplicationListener;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
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


        SpringApplication springApplication = builder.build();
        springApplication.addListeners(new ProfileApplicationListener());

        springApplication.run(args);

    }

}
