/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.monitor;

import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindVariableService;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcContext;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;


/**
 * @author Taejin Koo
 */
public class DefaultJdbcContext implements JdbcContext {

    private final JdbcUrlParsingService jdbcUrlParsingService;
    private final BindVariableService bindVariableService;

    @Inject
    public DefaultJdbcContext(JdbcUrlParsingService jdbcUrlParsingService, BindVariableService bindVariableService) {
        this.jdbcUrlParsingService = Objects.requireNonNull(jdbcUrlParsingService, "jdbcUrlParsingService");
        this.bindVariableService = Objects.requireNonNull(bindVariableService, "bindVariable");
    }


    @Override
    public DatabaseInfo parseJdbcUrl(ServiceType serviceType, String jdbcUrl) {
        return this.jdbcUrlParsingService.parseJdbcUrl(serviceType, jdbcUrl);
    }

    @Override
    public BindVariableService getBindVariableService() {
        return bindVariableService;
    }
}
