/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.util;

/**
 * @author Roy Kim
 */
public final class OsUtils {

    private static final OsType OS_TYPE = getType0();

    private OsUtils() {
    }

    public static OsType getType() {
        return OS_TYPE;
    }

    public static String getSystemProperty(SystemPropertyKey systemPropertyKey) {
        return System.getProperty(systemPropertyKey.getKey(), "");
    }

    private static OsType getType0() {
        String OsName = getSystemProperty(SystemPropertyKey.OS_NAME);
        return OsType.fromOsName(OsName);
    }
}
