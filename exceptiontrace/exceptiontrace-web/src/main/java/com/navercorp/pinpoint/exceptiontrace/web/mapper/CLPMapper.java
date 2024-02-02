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

/**
 * @author intr3p1d
 */
public class CLPMapper {

    // Special placeholder characters for CLP-encoded log type.
    public final static char DICTIONARY_VARIABLE_VALUE = '\u0011';
    public final static char NON_DICTIONARY_VALUE = '\u0012';

    public final static String DICTIONARY_REPLACEMENT = "▨▨▨";
    public final static String NON_DICTIONARY_REPLACEMENT = "▧▧▧";

    static String makeReadableString(String encodedLogType) {
        byte[] encodedLogTypeBytes = encodedLogType.getBytes(StandardCharsets.ISO_8859_1);
        return new String(encodedLogTypeBytes, StandardCharsets.UTF_8);
    }

    static String replacePlaceHolders(String encodedLogType) {
        return encodedLogType
                .replaceAll(String.valueOf(DICTIONARY_VARIABLE_VALUE), DICTIONARY_REPLACEMENT)
                .replaceAll(String.valueOf(NON_DICTIONARY_VALUE), NON_DICTIONARY_REPLACEMENT);
    }
}
