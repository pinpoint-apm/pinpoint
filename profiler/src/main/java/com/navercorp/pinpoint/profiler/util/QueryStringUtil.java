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

package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author emeroad
 */
public final class QueryStringUtil {

    private  QueryStringUtil() {
    }

    public static String removeCarriageReturn(String query) {
//        query.replaceAll(regex, replacement)
        String result = query.replaceAll("[\r\n]", " ");
        return result;
    }

    private static final Pattern MULTI_SPACE_ESCAPE = Pattern.compile(" +");

    public static String removeAllMultiSpace(String query) {
        if (StringUtils.isEmpty(query)) {
            return "";
        }
        Matcher matcher = MULTI_SPACE_ESCAPE.matcher(query);
        return matcher.replaceAll(" ");
    }
}
