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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.util.ArrayUtils;

public class DatabaseInfoAccessorUtils {

    public static DatabaseInfo getDatabaseInfo(Object[] array, int index) {
        if (!ArrayUtils.isArrayIndexValid(array, index)) {
            return null;
        }
        return getDatabaseInfo(array[index]);
    }

    public static DatabaseInfo getDatabaseInfo(Object object) {
        if (object instanceof DatabaseInfoAccessor) {
            return ((DatabaseInfoAccessor) object)._$PINPOINT$_getDatabaseInfo();
        }
        return null;
    }

    public static void setDatabaseInfo(final DatabaseInfo databaseInfo, final Object object) {
        if (object instanceof DatabaseInfoAccessor) {
            ((DatabaseInfoAccessor) object)._$PINPOINT$_setDatabaseInfo(databaseInfo);
        }
    }

    public static void setDatabaseInfo(final DatabaseInfo databaseInfo, final Object[] array, int index) {
        if (!ArrayUtils.isArrayIndexValid(array, index)) {
            return;
        }
        setDatabaseInfo(databaseInfo, array[index]);
    }
}
