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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import com.navercorp.pinpoint.bootstrap.util.PlatformClassLoaderUtils;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class SqlModule {
    private static final boolean SQL_MODULE;
    private static final Class<?> SQL_DATE;
    private static final Class<?> SQL_TIME;
    private static final Class<?> SQL_TIMESTAMP;
    private static final Class<?> SQL_CLOB;
    private static final Class<?> SQL_BLOB;

    private static final Class<?> SQL_CONNECTION;
    private static final Class<?> SQL_PREPARED_STATEMENT;

    static {
        if (JvmUtils.getVersion().onOrAfter(JvmVersion.JAVA_9)) {
            if (PlatformClassLoaderUtils.findClassFromPlatformClassLoader("java.sql.Date") != null) {
                SQL_MODULE = true;
            } else {
                SQL_MODULE = false;
            }
        } else {
            SQL_MODULE = true;
        }
        if (SQL_MODULE) {
            SQL_DATE = getSqlClass("java.sql.Date");
            SQL_TIME = getSqlClass("java.sql.Time");
            SQL_TIMESTAMP = getSqlClass("java.sql.Timestamp");
            SQL_CLOB = getSqlClass("java.sql.Clob");
            SQL_BLOB = getSqlClass("java.sql.Blob");

            SQL_CONNECTION = getSqlClass("java.sql.Connection");
            SQL_PREPARED_STATEMENT = getSqlClass("java.sql.PreparedStatement");
        } else {
            SQL_DATE = null;
            SQL_TIME = null;
            SQL_TIMESTAMP = null;
            SQL_CLOB = null;
            SQL_BLOB = null;

            SQL_CONNECTION = null;
            SQL_PREPARED_STATEMENT = null;
        }
    }

    private SqlModule() {
    }

    public static boolean isSqlModuleEnable() {
        return SQL_MODULE;
    }

    public static Class<?> getSqlDate() {
        return SQL_DATE;
    }

    public static Class<?> getSqlTime() {
        return SQL_TIME;
    }

    public static Class<?> getSqlTimestamp() {
        return SQL_TIMESTAMP;
    }

    public static Class<?> getSqlClob() {
        return SQL_CLOB;
    }

    public static Class<?> getSqlBlob() {
        return SQL_BLOB;
    }

    public static Class<?> getSqlConnection() {
        return SQL_CONNECTION;
    }

    public static Class<?> getSqlPreparedStatement() {
        return SQL_PREPARED_STATEMENT;
    }

    private static Class<?> getSqlClass(String className) {
        final Class<?> clazz = PlatformClassLoaderUtils.findClassFromPlatformClassLoader(className);
        if (clazz == null) {
            throw new IllegalStateException(className + " class not exist");
        }
        return clazz;
    }

}
