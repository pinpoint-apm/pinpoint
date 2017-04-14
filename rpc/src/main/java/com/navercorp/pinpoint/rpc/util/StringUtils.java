/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.util;

/**
 * @author Taejin Koo
 * @deprecated Since 1.7.0 Use {@link com.navercorp.pinpoint.common.util.StringUtils}
 */
@Deprecated
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * @deprecated Since 1.7.0 Use {@link com.navercorp.pinpoint.common.util.StringUtils#isEmpty(String)}
     */
    @Deprecated
    public static boolean isEmpty(String string) {
        return com.navercorp.pinpoint.common.util.StringUtils.isEmpty(string);
    }


    /**
     * @deprecated Since 1.7.0 Use org.apache.commons.lang3.StringUtils.equals(CharSequence, CharSequence)
     */
    @Deprecated
    public static boolean isEquals(String string1, String string2) {
        if (string1 == null) {
            return string2 == null;
        }

        return string1.equals(string2);
    }

}
