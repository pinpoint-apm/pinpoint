/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.tomcat.jdbc;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author Taejin Koo
 */
public final class TomcatJdbcConstants {
    private TomcatJdbcConstants() {
    }

    public static final String SCOPE = "TOMCATJDBC_SCOPE";

    public static final ServiceType SERVICE_TYPE = ServiceTypeFactory.of(6050, "TOMCATJDBC");

    public static final String ACCESSOR_DATASOURCE_MONITOR = "com.navercorp.pinpoint.plugin.tomcat.jdbc.DataSourceMonitorAccessor";

    public static final String INTERCEPTOR_CONSTRUCTOR = "com.navercorp.pinpoint.plugin.tomcat.jdbc.interceptor.DataSourceConstructorInterceptor";
    public static final String INTERCEPTOR_CLOSE = "com.navercorp.pinpoint.plugin.tomcat.jdbc.interceptor.DataSourceCloseInterceptor";

    public static final String INTERCEPTOR_GET_CONNECTION = "com.navercorp.pinpoint.plugin.tomcat.jdbc.interceptor.DataSourceGetConnectionInterceptor";
    public static final String INTERCEPTOR_CLOSE_CONNECTION = "com.navercorp.pinpoint.plugin.tomcat.jdbc.interceptor.DataSourceCloseConnectionInterceptor";

}
