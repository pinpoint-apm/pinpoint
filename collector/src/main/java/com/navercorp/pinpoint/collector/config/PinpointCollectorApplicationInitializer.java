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

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.server.profile.Log4j2ProfileProperty;
import com.navercorp.pinpoint.common.server.profile.ProfileApplicationInitializer;
import com.navercorp.pinpoint.common.util.SystemProperty;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PinpointCollectorApplicationInitializer implements WebApplicationInitializer {

    public PinpointCollectorApplicationInitializer() {
    }


    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        String defaultProfile = servletContext.getInitParameter(ProfileApplicationInitializer.PINPOINT_DEFAULT_ACTIVE_PROFILE_KEY);
        ProfileApplicationInitializer initializer = new ProfileApplicationInitializer(getClass().getSimpleName(), SystemProperty.INSTANCE, defaultProfile);
        initializer.onStartup();

        // log4j2 initialize
        final String log4jConfigurationProfile = servletContext.getInitParameter(Log4j2ProfileProperty.LOG4J2_CONFIGURATION_PROFILE);
        if (log4jConfigurationProfile != null) {
            Log4j2ProfileProperty log4J2ProfileProperty = new Log4j2ProfileProperty();
            // Log4jServletContextListener does not support placeHolder
            String log4jConfiguration = log4J2ProfileProperty.getLog4jProfileConfiguration(log4jConfigurationProfile);
            servletContext.setInitParameter(Log4j2ProfileProperty.LOG4J2_CONFIGURATION, log4jConfiguration);
        }
    }

}