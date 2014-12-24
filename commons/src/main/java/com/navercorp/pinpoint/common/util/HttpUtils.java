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

/**
 * @author emeroad
 */
public final class HttpUtils {

    private static final String UTF8 = "UTF-8";

    private static final String CHARSET = "charset=";

    private HttpUtils() {
    }

    public static String parseContentTypeCharset(String contentType) {
        return parseContentTypeCharset(contentType, UTF8);
    }

    public static String parseContentTypeCharset(String contentType, String defaultCharset) {
        if (contentType == null) {
            // 스펙상으로는 iso-8859-1 이나 요즘 대부분 was에서 UTF-8 고치기 때문에 애매하다. 옵션 설정에서 고칠수 있게 해야 될지도 모름.
            return defaultCharset;
        }
        int charsetStart = contentType.indexOf(CHARSET);
        if (charsetStart == -1) {
            // 없음.
            return defaultCharset;
        }
        // 요기가 시작점.
        charsetStart = charsetStart + CHARSET.length();
        int charsetEnd = contentType.indexOf(';', charsetStart);
        if (charsetEnd == -1) {
            charsetEnd = contentType.length();
        }
        contentType = contentType.substring(charsetStart, charsetEnd);

        return contentType.trim();
    }
}
