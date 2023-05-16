/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.env;

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
