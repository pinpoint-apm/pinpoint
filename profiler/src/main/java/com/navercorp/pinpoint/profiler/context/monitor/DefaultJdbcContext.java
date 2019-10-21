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
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;


/**
 * @author Taejin Koo
 */
public class DefaultJdbcContext implements JdbcContext {

    private final JdbcUrlParsingService jdbcUrlParsingService;

    @Inject
    public DefaultJdbcContext(JdbcUrlParsingService jdbcUrlParsingService) {
        this.jdbcUrlParsingService = Assert.requireNonNull(jdbcUrlParsingService, "jdbcUrlParsingService");
    }


    @Override
    public DatabaseInfo parseJdbcUrl(ServiceType serviceType, String jdbcUrl) {
        return this.jdbcUrlParsingService.parseJdbcUrl(serviceType, jdbcUrl);
    }


}
