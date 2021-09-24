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

package com.navercorp.pinpoint.common.server.starter;

import com.navercorp.pinpoint.common.server.env.EnvironmentLoggingListener;
import com.navercorp.pinpoint.common.server.env.ExternalEnvironmentListener;
import com.navercorp.pinpoint.common.server.env.ProfileResolveListener;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Objects;

public class BasicStarter {
    protected String externalPropertySourceName;
    protected String externalConfigurationKey;

    private final Class<?>[] sources;

    public BasicStarter(Class<?>... sources) {
        this.sources = Objects.requireNonNull(sources, "sources");
    }

    public void start(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder();

        builder.sources(sources);
        builder.web(WebApplicationType.SERVLET);
        builder.bannerMode(Banner.Mode.OFF);

        builder.listeners(new ProfileResolveListener());
        builder.listeners(new EnvironmentLoggingListener());
        builder.listeners(new ExternalEnvironmentListener(externalPropertySourceName, externalConfigurationKey));

        SpringApplication springApplication = builder.build();
        springApplication.run(args);

    }
}
