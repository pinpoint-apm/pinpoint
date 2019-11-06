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

package com.navercorp.pinpoint.bootstrap.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public final class InterceptorUtils {
    private InterceptorUtils() {
    }

    public static boolean isThrowable(Object result) {
        return result instanceof Throwable;
    }

    public static boolean isSuccess(Throwable throwable) {
        return throwable == null;
    }


    public static String exceptionToString(Throwable ex) {
        if (ex != null) {
            StringBuilder sb = new StringBuilder(128);
            sb.append(ex.toString()).append('\n');

            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            sb.append(writer.toString());

            return sb.toString();
        }
        return null;
    }

    public static String getHttpUrl(final String uriString, final boolean param) {
        if (com.navercorp.pinpoint.common.util.StringUtils.isEmpty(uriString)) {
            return "";
        }

        if (param) {
            return uriString;
        }

        int queryStart = uriString.indexOf('?');
        if (queryStart != -1) {
            return uriString.substring(0, queryStart);
        }

        return uriString;
    }
}
