/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;

public class SetDatabaseInfoConstructorInterceptor extends SetDatabaseInfoInterceptor {
    private final int argumentIndex;

    public SetDatabaseInfoConstructorInterceptor(int argumentIndex) {
        this.argumentIndex = argumentIndex;
    }

    @Override
    public DatabaseInfo getDatabaseInfo(Object target, Object[] args, Object result) {
        final DatabaseInfoAccessor databaseInfoAccessor = ArrayArgumentUtils.getArgument(args, argumentIndex, DatabaseInfoAccessor.class);
        if (databaseInfoAccessor == null) {
            return null;
        }
        return databaseInfoAccessor._$PINPOINT$_getDatabaseInfo();
    }

    @Override
    public boolean setDatabaseInfo(DatabaseInfo databaseInfo, Object target, Object[] args, Object result) {
        if (target instanceof DatabaseInfoAccessor) {
            ((DatabaseInfoAccessor) target)._$PINPOINT$_setDatabaseInfo(databaseInfo);
            return true;
        }
        return false;
    }
}
