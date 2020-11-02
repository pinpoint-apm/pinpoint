package com.navercorp.pinpoint.collector;

import com.navercorp.pinpoint.common.server.profile.ProfileApplicationListener;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

public class CollectorStarter {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(CollectorApp.class);

    private final SpringApplicationBuilder builder;

    public CollectorStarter(Class<?>... sources) {
        this.builder = new SpringApplicationBuilder();
        this.builder.web(WebApplicationType.SERVLET);
        this.builder.bannerMode(Banner.Mode.OFF);

        builder.sources(sources);
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
