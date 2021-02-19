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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc.bindvalue;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

import com.navercorp.pinpoint.bootstrap.plugin.jdbc.SqlModule;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author emeroad
 */
public class ObjectConverter implements Converter {
    private static final boolean SQL_MODULE;
    private static final Class<?> SQL_DATE;
    private static final Class<?> SQL_TIME;
    private static final Class<?> SQL_TIMESTAMP;
    private static final Class<?> SQL_CLOB;
    private static final Class<?> SQL_BLOB;

    static {
        SQL_MODULE = SqlModule.isSqlModuleEnable();
        SQL_DATE = SqlModule.getSqlDate();
        SQL_TIME = SqlModule.getSqlTime();
        SQL_TIMESTAMP = SqlModule.getSqlTimestamp();
        SQL_CLOB = SqlModule.getSqlClob();
        SQL_BLOB = SqlModule.getSqlBlob();
    }


    @Override
    public String convert(Object[] args) {
        if (args == null) {
            return "null";
        }
        if (args.length == 2) {
            final Object param = args[1];
            return getParameter(param);
        }
        if (args.length == 3) {
            final Object param = args[1];
            return getParameter(param);
        }
        return "error";
    }

    private String getParameter(Object param) {
        if (param == null) {
            return "null";
        }
        if (param instanceof String) {
            return abbreviate(param);
        }
        if (param instanceof Boolean) {
            return toString(param);
        }
        if (param instanceof Integer) {
            return toString(param);
        }
        if (param instanceof Long) {
            return toString(param);
        }
        if (param instanceof Short) {
            return toString(param);
        }
        if (param instanceof Float) {
            return toString(param);
        }
        if (param instanceof Double) {
            return toString(param);
        }
        if (param instanceof Byte) {
            return toString(param);
        }
        if (param instanceof UUID) {
            return toString(param);
        }

        if (param instanceof byte[]) {
            return ArrayUtils.abbreviate((byte[]) param);
        }
        if (param instanceof InputStream) {
            return getClassName(param);
        }

        if (param instanceof BigDecimal) {
            return toString(param);
        }
        if (param instanceof BigInteger) {
            return toString(param);
        }



        if (SQL_MODULE) {
            if (SQL_DATE.isInstance(param)) {
                return toString(param);
            }
            if (SQL_TIME.isInstance(param)) {
                return toString(param);
            }
            if (SQL_TIMESTAMP.isInstance(param)) {
                return toString(param);
            }
            if (SQL_BLOB.isInstance(param)) {
                return getClassName(param);
            }
            if (SQL_CLOB.isInstance(param)) {
                return getClassName(param);
            }
        }
        return getClassName(param);
    }

    private String abbreviate(Object param) {
        return StringUtils.abbreviate(param.toString());
    }

    private String toString(Object param) {
        return param.toString();
    }


    private String getClassName(Object param) {
        return param.getClass().getName();
    }
}
