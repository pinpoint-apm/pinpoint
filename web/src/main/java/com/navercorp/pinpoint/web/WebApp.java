/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import com.navercorp.pinpoint.datasource.MainDataSourcePropertySource;
import com.navercorp.pinpoint.login.basic.PinpointBasicLoginConfig;
import com.navercorp.pinpoint.redis.RedisPropertySources;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class
})
@Import({
        PinpointWebModule.class,
        MainDataSourcePropertySource.class,
        RedisPropertySources.class,
})
public class WebApp  {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(WebApp.class);

    public static void main(String[] args) {
        try {
            WebStarter starter = new WebStarter(WebApp.class, PinpointBasicLoginConfig.class, AuthorizationConfig.class);
            starter.start(args);
        } catch (Exception exception) {
            logger.error("[WebApp] could not launch app.", exception);
        }
    }

}
