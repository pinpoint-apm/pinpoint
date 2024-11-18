/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.exceptiontrace.web.mapper;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author intr3p1d
 */
public class CLPMapper {

    // Special placeholder characters for CLP-encoded log type.
    public final static char DICTIONARY_VARIABLE_VALUE = '\u0011';
    public final static char NON_DICTIONARY_VALUE = '\u0012';

    public final static String DICTIONARY_REPLACEMENT = "<replaced>";
    public final static String NON_DICTIONARY_REPLACEMENT = "<replaced>";

    static String makeReadableString(String encodedLogType) {
        byte[] encodedLogTypeBytes = encodedLogType.getBytes(StandardCharsets.ISO_8859_1);
        return new String(encodedLogTypeBytes, StandardCharsets.UTF_8);
    }

    private static final Pattern PATTERN = Pattern.compile(
            String.format("(%s).{1}|(%s).{1}", DICTIONARY_VARIABLE_VALUE, NON_DICTIONARY_VALUE)
    );

    static String replacePlaceHolders(String encodedLogType) {
        Matcher matcher = PATTERN.matcher(encodedLogType);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                matcher.appendReplacement(result, DICTIONARY_REPLACEMENT);
            } else if (matcher.group(2) != null) {
                matcher.appendReplacement(result, NON_DICTIONARY_REPLACEMENT);
            }
        }

        if (result.isEmpty()) {
            return encodedLogType;
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
