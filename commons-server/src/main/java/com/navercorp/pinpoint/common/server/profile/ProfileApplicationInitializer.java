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

package com.navercorp.pinpoint.common.server.profile;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.common.util.SystemProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ProfileApplicationInitializer {


    // refer to : org.springframework.core.env.AbstractEnvironment
    public static final String IGNORE_GETENV_PROPERTY_NAME = "spring.getenv.ignore";
    public static final String ACTIVE_PROFILES_PROPERTY_NAME = "spring.profiles.active";
    public static final String ACTIVE_PROFILES_PROPERTY_ENV_NAME = "SPRING_PROFILES_ACTIVE";

    public static final String PINPOINT_DEFAULT_PROFILE = "release";

    public static final String PINPOINT_DEFAULT_ACTIVE_PROFILE_KEY = "pinpoint.default.active.profile";
    public static final String PINPOINT_ACTIVE_PROFILE = "pinpoint.profiles.active";
    // TODO
    public static final String PINPOINT_ACTIVE_OPTIONAL_PROFILE = "pinpoint.profiles.optional";

    private final String name;
    private final SystemProperty systemProperty;
    private final String defaultProfile;


    public ProfileApplicationInitializer(String name, SystemProperty systemProperty, String defaultProfile) {
        this.name = Objects.requireNonNull(name, "name");
        this.systemProperty = Objects.requireNonNull(systemProperty, "systemProperty");
        this.defaultProfile = getDefaultProfile(defaultProfile);
    }

    public void onStartup() {
        String activeProfile = this.systemProperty.getProperty(ACTIVE_PROFILES_PROPERTY_NAME);
        if (activeProfile == null) {
            if (!suppressGetenvAccess()) {
                activeProfile = this.systemProperty.getEnv(ACTIVE_PROFILES_PROPERTY_ENV_NAME);
            }
        }
        if (activeProfile == null) {
            activeProfile = this.defaultProfile;
        }
        LocalDateTime now = LocalDateTime.now();
        final String activeProfileMessage = String.format("%s %s::ActiveProfile:%s", now, name, activeProfile);
        System.out.println(activeProfileMessage);

        List<String> profileList = StringUtils.tokenizeToStringList(activeProfile, ",");
        // TODO exclusive profile(local or release)
        this.systemProperty.setProperty(PINPOINT_ACTIVE_PROFILE, profileList.get(0));
        this.systemProperty.setProperty(ACTIVE_PROFILES_PROPERTY_NAME, activeProfile);

    }

    private String getDefaultProfile(String defaultProfile) {
        if (StringUtils.isEmpty(defaultProfile)) {
            return PINPOINT_DEFAULT_PROFILE;
        }
        return defaultProfile;
    }

     private boolean suppressGetenvAccess() {
        String ignore = this.systemProperty.getEnv(IGNORE_GETENV_PROPERTY_NAME);
        return Boolean.parseBoolean(ignore);
    }
}