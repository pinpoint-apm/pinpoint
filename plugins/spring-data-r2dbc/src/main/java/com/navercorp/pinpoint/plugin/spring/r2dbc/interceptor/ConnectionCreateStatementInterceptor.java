/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;

public class ConnectionCreateStatementInterceptor extends SetDatabaseInfoInterceptor {

    private final TraceContext traceContext;

    public ConnectionCreateStatementInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public DatabaseInfo getDatabaseInfo(Object target, Object[] args, Object result) {
        if (target instanceof DatabaseInfoAccessor) {
            final DatabaseInfoAccessor databaseInfoAccessor = (DatabaseInfoAccessor) target;
            return databaseInfoAccessor._$PINPOINT$_getDatabaseInfo();
        }
        return null;
    }

    @Override
    public boolean setDatabaseInfo(DatabaseInfo databaseInfo, Object target, Object[] args, Object result) {
        if (Boolean.FALSE == (result instanceof DatabaseInfoAccessor)) {
            return false;
        }
        ((DatabaseInfoAccessor) result)._$PINPOINT$_setDatabaseInfo(databaseInfo);
        final Trace trace = traceContext.currentTraceObject();
        if (trace != null) {
            setParsingResult(args, result);
        }

        return true;
    }

    void setParsingResult(Object[] args, Object result) {
        if (result instanceof ParsingResultAccessor) {
            // 1. Don't check traceContext. preparedStatement can be created in other thread.
            // 2. While sampling is active, the thread which creates preparedStatement could not be a sampling target. So record sql anyway.
            final String sql = ArrayArgumentUtils.getArgument(args, 0, String.class);
            if (sql != null) {
                ParsingResult parsingResult = traceContext.parseSql(sql);
                if (parsingResult != null) {
                    ((ParsingResultAccessor) result)._$PINPOINT$_setParsingResult(parsingResult);
                } else {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Failed to parse sql. sql={}", sql);
                    }
                }
            }
        }
    }
}
