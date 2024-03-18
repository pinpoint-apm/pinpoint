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

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindVariableService;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.jdbc.BindValueConverter;
import com.navercorp.pinpoint.profiler.jdbc.DefaultBindVariableService;

/**
 * @author Taejin Koo
 */
public final class DisabledJdbcContext implements JdbcContext {

    public static final DisabledJdbcContext INSTANCE = new DisabledJdbcContext();
    private final BindVariableService bindVariableService;

    public DisabledJdbcContext() {

        BindValueConverter bindValueConverter = BindValueConverter.defaultBindValueConverter();
        this.bindVariableService = new DefaultBindVariableService(bindValueConverter);
    }

    @Override
    public DatabaseInfo parseJdbcUrl(ServiceType serviceType, String jdbcUrl) {
        return UnKnownDatabaseInfo.createUnknownDataBase(jdbcUrl);
    }

    @Override
    public BindVariableService getBindVariableService() {
        return bindVariableService;
    }

}
