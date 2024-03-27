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

package com.navercorp.pinpoint.it.plugin.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public final class LogUtils {
    private static final String LINE_BREAK_REGEX = "((\\r?\\n)|(\\r))";
    private static final String LINE_BREAK_AT_END_REGEX = LINE_BREAK_REGEX + "$";
    private static final Pattern LINE_BREAK_AT_END_REGEX_PATTERN = Pattern.compile(LINE_BREAK_AT_END_REGEX);

    private LogUtils() {
    }

    /**
     * @deprecated Since 3.0.0 Use {@link org.testcontainers.containers.output.OutputFrame#getUtf8StringWithoutLineEnding}
     */
    @Deprecated
    public static String removeLineBreak(String log) {
        Matcher matcher = LINE_BREAK_AT_END_REGEX_PATTERN.matcher(log);
        return matcher.replaceAll("");
    }
}
