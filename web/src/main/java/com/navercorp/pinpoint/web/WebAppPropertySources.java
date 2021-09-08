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

package com.navercorp.pinpoint.web;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;


@PropertySources({
        @PropertySource(name = "WebAppPropertySources", value = { WebAppPropertySources.WEB_ROOT, WebAppPropertySources.WEB_PROFILE}),
        @PropertySource(name = "WebAppPropertySources-HBase", value = { WebAppPropertySources.HBASE_ROOT, WebAppPropertySources.HBASE_PROFILE}),
        @PropertySource(name = "WebAppPropertySources-JDBC", value = { WebAppPropertySources.JDBC_ROOT, WebAppPropertySources.JDBC_PROFILE}),
})
public final class WebAppPropertySources {
    public static final String HBASE_ROOT= "classpath:hbase-root.properties";
    public static final String HBASE_PROFILE = "classpath:profiles/${pinpoint.profiles.active:release}/hbase.properties";

    public static final String JDBC_ROOT = "classpath:jdbc-root.properties";
    public static final String JDBC_PROFILE = "classpath:profiles/${pinpoint.profiles.active:release}/jdbc.properties";

    public static final String WEB_ROOT = "classpath:pinpoint-web-root.properties";
    public static final String WEB_PROFILE = "classpath:profiles/${pinpoint.profiles.active:release}/pinpoint-web.properties";

    private WebAppPropertySources() {
    }
}
