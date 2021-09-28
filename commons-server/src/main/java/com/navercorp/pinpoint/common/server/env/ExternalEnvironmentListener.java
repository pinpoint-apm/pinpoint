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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Objects;

public class ExternalEnvironmentListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String sourceName;
    private final String configurationKey;

    public ExternalEnvironmentListener(String sourceName, String configurationKey) {
        this.sourceName = Objects.requireNonNull(sourceName, "sourceName");
        this.configurationKey = Objects.requireNonNull(configurationKey, "configurationKey");
    }


    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {


        logger.info("onApplicationEvent({}})", event.getClass().getSimpleName());

        ConfigurableEnvironment environment = event.getEnvironment();

        ExternalEnvironment externalEnvironment = new ExternalEnvironment(sourceName, configurationKey);
        externalEnvironment.processEnvironment(environment);

    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
