/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.common.util.SimpleProperty;

import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PropertyLoaderFactory {

    private final SimpleProperty javaSystemProperty;
    private final SimpleProperty osEnvProperty;

    private final Path agentRootPath;

    // @Optional
    private final Path profilesPath;
    // @Optional
    private final String[] supportedProfiles;

    public PropertyLoaderFactory(SimpleProperty javaSystemProperty, SimpleProperty osEnvProperty,
                                 Path agentRootPath, Path profilesPath, String[] supportedProfiles) {
        this.javaSystemProperty = Objects.requireNonNull(javaSystemProperty, "javaSystemProperty");
        this.osEnvProperty = Objects.requireNonNull(osEnvProperty, "osEnvProperty");
        this.agentRootPath = Objects.requireNonNull(agentRootPath, "agentRootPath");
        this.profilesPath = Objects.requireNonNull(profilesPath, "profilesPath");
        this.supportedProfiles = Objects.requireNonNull(supportedProfiles, "supportedProfiles");
    }

    public PropertyLoader newPropertyLoader() {
        if (isSimpleMode()) {
            return new SimplePropertyLoader(javaSystemProperty, agentRootPath, profilesPath);
        }
        return new ProfilePropertyLoader(javaSystemProperty, osEnvProperty, agentRootPath, profilesPath, supportedProfiles);
    }



    private boolean isSimpleMode() {
        final String mode = javaSystemProperty.getProperty(Profiles.CONFIG_LOAD_MODE_KEY, Profiles.CONFIG_LOAD_MODE.PROFILE.toString());
        return Profiles.CONFIG_LOAD_MODE.SIMPLE.toString().equalsIgnoreCase(mode);
    }
}
