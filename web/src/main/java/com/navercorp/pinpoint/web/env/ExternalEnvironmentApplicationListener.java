package com.navercorp.pinpoint.web.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

public class ExternalEnvironmentApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {


        logger.info("onApplicationEvent({}})", event.getClass().getSimpleName());

        ConfigurableEnvironment environment = event.getEnvironment();
        SpringApplication springApplication = event.getSpringApplication();

        EnvironmentPostProcessor externalProcessor = new ExternalEnvironmentPostProcessor();
        externalProcessor.postProcessEnvironment(environment, springApplication);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
