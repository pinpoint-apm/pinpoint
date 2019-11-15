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

import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.time.LocalDateTime;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ProfileApplicationInicationInitializer implements WebApplicationInitializer {


    // refer to : org.springframework.core.env.AbstractEnvironment
    public static final String IGNORE_GETENV_PROPERTY_NAME = "spring.getenv.ignore";
    public static final String ACTIVE_PROFILES_PROPERTY_NAME = "spring.profiles.active";

    public static final String PINPOINT_DEFAULT_PROFILE = "release";

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        String activeProfile = System.getProperty(ACTIVE_PROFILES_PROPERTY_NAME);
        if (activeProfile == null) {
            if (!suppressGetenvAccess()) {
                activeProfile = System.getenv(ACTIVE_PROFILES_PROPERTY_NAME);
            }
        }
        if (activeProfile == null) {
            activeProfile = PINPOINT_DEFAULT_PROFILE;
        }
        LocalDateTime now = LocalDateTime.now();
        final String activeProfileMessage = String.format("%s PINPOINT::ActiveProfile:%s", now, activeProfile);
        System.out.println(activeProfileMessage);
        servletContext.setInitParameter(ACTIVE_PROFILES_PROPERTY_NAME, activeProfile);
        System.setProperty(ACTIVE_PROFILES_PROPERTY_NAME, activeProfile);

    }

    private boolean suppressGetenvAccess() {
        String ignore = System.getenv(IGNORE_GETENV_PROPERTY_NAME);
        return Boolean.parseBoolean(ignore);
    }
}