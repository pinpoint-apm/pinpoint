package com.navercorp.pinpoint.collector.starter.multi.module;

import com.navercorp.pinpoint.collector.env.CollectorEnvironmentApplicationListener;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Deprecated
public class MultiStarter {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(MultiModuleApp.class);

    private final SpringApplicationBuilder builder;
    private List<SpringApplicationBuilder> builderList = new ArrayList<>();

    public MultiStarter(Class<?>... sources) {
        this.builder = new SpringApplicationBuilder();
        this.builder.web(WebApplicationType.SERVLET);
        this.builder.bannerMode(Banner.Mode.OFF);

        builder.sources(sources);
    }

    public void addModule(Class<?> child, ApplicationListener<?> listener, int port) {
        builderList.add(builderForSource(child, port).listeners(listener));
    }

    public void addModule(Class<?> child, int port) {
        builderList.add(builderForSource(child, port));
    }

    private SpringApplicationBuilder builderForSource(Class<?> child, int port) {
        Objects.requireNonNull(child, "child");
        return this.builder.child(child)
                .web(WebApplicationType.SERVLET)
                .bannerMode(Banner.Mode.OFF)
                .listeners(new CollectorEnvironmentApplicationListener())
                .properties(portProperty(port));
    }

    private String portProperty(int port) {
        return new StringBuilder("server.port:").append(port).toString();
    }

    public void start(String[] args) {
        for (SpringApplicationBuilder builder : builderList) {
            builder.build().run(args);
        }
    }

}
