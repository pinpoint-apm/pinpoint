/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.log.web;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import static com.navercorp.pinpoint.log.web.LogWebPropertySources.LOG;
import static com.navercorp.pinpoint.log.web.LogWebPropertySources.LOG_ROOT;

@PropertySources({
        @PropertySource(name = "LogPropertySources", value = { LOG_ROOT, LOG }),
})
public class LogWebPropertySources {

    private static final String PROFILE = "classpath:log/profiles/${pinpoint.profiles.active:local}/";

    public static final String LOG_ROOT = "classpath:log/pinpoint-web-log-root.properties";
    public static final String LOG = PROFILE + "pinpoint-web-log.properties";

}
