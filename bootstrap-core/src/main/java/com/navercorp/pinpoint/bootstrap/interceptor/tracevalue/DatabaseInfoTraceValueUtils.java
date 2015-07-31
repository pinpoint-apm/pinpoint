/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.interceptor.tracevalue;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;

/**
 * @author emeroad
 */
public final class DatabaseInfoTraceValueUtils {

    private DatabaseInfoTraceValueUtils() {
    }

    public static DatabaseInfo __getTraceDatabaseInfo(Object target, DatabaseInfo defaultValue) {
        if (target == null) {
            return defaultValue;
        }
        if (target instanceof DatabaseInfoTraceValue) {
            final DatabaseInfo databaseInfo = ((DatabaseInfoTraceValue) target)._$PINPOINT$_getTraceDatabaseInfo();
            if (databaseInfo == null) {
                return defaultValue;
            }
            return databaseInfo;
        }
        return defaultValue;
    }

    public static void __setTraceDatabaseInfo(Object target, DatabaseInfo databaseInfo) {
        if (target == null) {
            return;
        }
        if (target instanceof DatabaseInfoTraceValue) {
            ((DatabaseInfoTraceValue) target)._$PINPOINT$_setTraceDatabaseInfo(databaseInfo);
        }
    }
}
