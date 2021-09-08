package com.navercorp.pinpoint.web.env;

import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

public class ProfileResolveApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    private final ServerBootLogger logger = ServerBootLogger.getLogger(getClass());

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        logger.info(String.format("onApplicationEvent(%s)", event.getClass().getSimpleName()));

        ConfigurableEnvironment environment = event.getEnvironment();
        SpringApplication springApplication = event.getSpringApplication();

        ProfileEnvironmentPostProcessor profileEnvironment = new ProfileEnvironmentPostProcessor();
        profileEnvironment.postProcessEnvironment(environment, springApplication);

    }

    /**
     * @see org.springframework.boot.context.logging.LoggingApplicationListener#DEFAULT_ORDER
     * @see ConfigDataEnvironmentPostProcessor#ORDER
     */
    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

}
