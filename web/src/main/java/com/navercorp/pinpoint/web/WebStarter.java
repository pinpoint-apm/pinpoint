package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.common.server.profile.ProfileApplicationListener;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

public class WebStarter {
    private final ServerBootLogger logger = ServerBootLogger.getLogger(WebApp.class);

    private final SpringApplicationBuilder builder;

    public WebStarter(Class<?>... sources) {
        this.builder = new SpringApplicationBuilder();
        this.builder.web(WebApplicationType.SERVLET);
        this.builder.bannerMode(Banner.Mode.OFF);

        builder.sources(sources);
    }

    public void sibling(Class<?>... childs) {
        this.builder.sibling(childs);
    }

    public void child(Class<?>... childs) {
        this.builder.child(childs);
    }

    public void start(String[] args) {
        SpringApplication springApplication = builder.build();
        springApplication.addListeners(new ProfileApplicationListener());

        springApplication.run(args);

    }

}
