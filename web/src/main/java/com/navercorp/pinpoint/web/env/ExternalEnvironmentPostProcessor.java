package com.navercorp.pinpoint.web.env;

import com.navercorp.pinpoint.common.server.env.ExternalEnvironment;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;


public class ExternalEnvironmentPostProcessor implements EnvironmentPostProcessor {

    public static final String EXTERNAL_PROPERTY_SOURCE_NAME = "WebExternalEnvironment";

    private static final String EXTERNAL_CONFIGURATION_KEY = "pinpoint.web.config.location";

    public ExternalEnvironmentPostProcessor() {
    }


    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("postProcessEnvironment");


        ExternalEnvironment externalEnvironment
                = new ExternalEnvironment(EXTERNAL_PROPERTY_SOURCE_NAME, EXTERNAL_CONFIGURATION_KEY);
        externalEnvironment.processEnvironment(environment);

    }



}
