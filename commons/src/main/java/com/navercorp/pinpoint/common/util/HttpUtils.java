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

package com.navercorp.pinpoint.common.util;

import com.navercorp.pinpoint.common.Charsets;

/**
 * @author emeroad
 */
public final class HttpUtils {

    private static final String UTF8 = Charsets.UTF_8_NAME;

    private static final String CHARSET = "charset=";

    private HttpUtils() {
    }

    public static String parseContentTypeCharset(String contentType) {
        return parseContentTypeCharset(contentType, UTF8);
    }

    public static String parseContentTypeCharset(String contentType, String defaultCharset) {
        if (contentType == null) {
            // default spec specifies iso-8859-1, but most WASes set it to UTF-8.
            // might be better to make it configurable
            return defaultCharset;
        }
        int charsetStart = contentType.indexOf(CHARSET);
        if (charsetStart == -1) {
            // none
            return defaultCharset;
        }
        charsetStart = charsetStart + CHARSET.length();
        int charsetEnd = contentType.indexOf(';', charsetStart);
        if (charsetEnd == -1) {
            charsetEnd = contentType.length();
        }
        contentType = contentType.substring(charsetStart, charsetEnd);

        return contentType.trim();
    }
}
