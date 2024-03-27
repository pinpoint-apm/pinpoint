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

import com.navercorp.pinpoint.common.server.banner.PinpointSpringBanner;
import com.navercorp.pinpoint.common.server.env.AdditionalProfileListener;
import com.navercorp.pinpoint.common.server.env.EnvironmentLoggingListener;
import com.navercorp.pinpoint.common.server.env.ExternalEnvironmentListener;
import com.navercorp.pinpoint.common.server.env.ProfileResolveListener;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BasicStarter {
    private final String externalPropertySourceName;
    private final String externalConfigurationKey;
    private final List<String> externalProfiles = new ArrayList<>();

    private WebApplicationType webApplicationType = WebApplicationType.SERVLET;

    private final Class<?>[] sources;

    public BasicStarter(String externalPropertySourceName, String externalConfigurationKey, Class<?>... sources) {
        this.externalPropertySourceName = Objects.requireNonNull(externalPropertySourceName, "externalPropertySourceName");
        this.externalConfigurationKey = Objects.requireNonNull(externalConfigurationKey, "externalConfigurationKey");
        this.sources = Objects.requireNonNull(sources, "sources");
    }

    public void addProfiles(String ...profiles) {
        externalProfiles.addAll(List.of(profiles));
    }

    public void setWebApplicationType(WebApplicationType webApplicationType) {
        this.webApplicationType = Objects.requireNonNull(webApplicationType, "webApplicationType");
    }

    public void start(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder();

        builder.sources(sources);
        builder.web(webApplicationType);
        builder.bannerMode(Banner.Mode.OFF);

        builder.listeners(new AdditionalProfileListener(externalProfiles));
        builder.listeners(new ProfileResolveListener());
        builder.listeners(new EnvironmentLoggingListener());
        builder.listeners(new ExternalEnvironmentListener(externalPropertySourceName, externalConfigurationKey));
        builder.listeners(new PinpointSpringBanner());

        SpringApplication springApplication = builder.build();
        springApplication.run(args);

    }
}
