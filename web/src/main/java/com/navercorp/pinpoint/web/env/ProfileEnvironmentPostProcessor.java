package com.navercorp.pinpoint.web.env;

import com.navercorp.pinpoint.common.server.profile.PinpointProfileEnvironment;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Objects;

public class ProfileEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private final ServerBootLogger logger = ServerBootLogger.getLogger(getClass());

    private final String defaultProfile;

    public ProfileEnvironmentPostProcessor() {
        this.defaultProfile = null;
    }

    public ProfileEnvironmentPostProcessor(String defaultProfile) {
        this.defaultProfile = Objects.requireNonNull(defaultProfile, "defaultProfile");
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        logger.info("postProcessEnvironment");

        PinpointProfileEnvironment profileEnvironment = newProfileEnvironment();
        profileEnvironment.processEnvironment(environment);
    }



    private PinpointProfileEnvironment newProfileEnvironment() {
        if (defaultProfile == null) {
            return new PinpointProfileEnvironment();
        }
        return new PinpointProfileEnvironment(defaultProfile);
    }

}
