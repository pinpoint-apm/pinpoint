/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.jdbc.mariadb.interceptor;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessorUtils;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;

public class PreparedStatementConstructorInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());

    private TraceContext traceContext;

    public PreparedStatementConstructorInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (throwable != null) {
            return;
        }

        try {
            final DatabaseInfo databaseInfo = DatabaseInfoAccessorUtils.getDatabaseInfo(args, 1);
            if (databaseInfo != null) {
                DatabaseInfoAccessorUtils.setDatabaseInfo(databaseInfo, target);
            }
            if (target instanceof ParsingResultAccessor) {
                // 1. Don't check traceContext. preparedStatement can be created in other thread.
                // 2. While sampling is active, the thread which creates preparedStatement could not be a sampling target. So record sql anyway.
                final String sql = ArrayArgumentUtils.getArgument(args, 0, String.class);
                if (sql != null) {
                    ParsingResult parsingResult = traceContext.parseSql(sql);
                    if (parsingResult != null) {
                        ((ParsingResultAccessor) target)._$PINPOINT$_setParsingResult(parsingResult);
                    }
                }
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        }
    }
}