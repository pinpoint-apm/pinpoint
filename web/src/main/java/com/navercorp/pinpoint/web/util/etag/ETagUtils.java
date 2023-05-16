/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.util.etag;

/**
 * copy from io.undertow.util.ETagUtils
 */
public class ETagUtils {

    private static final char COMMA = ',';
    private static final char QUOTE = '"';
    private static final char W = 'W';
    private static final char SLASH = '/';

    public static ETag parseETag(final String tag) {
        if (tag == null) {
            return null;
        }
        char[] headerChars = tag.toCharArray();
        SearchingFor searchingFor = SearchingFor.START_OF_VALUE;
        int valueStart = 0;
        boolean weak = false;
        boolean malformed = false;
        for (int i = 0; i < headerChars.length; i++) {
            switch (searchingFor) {
                case START_OF_VALUE:
                    if (headerChars[i] != COMMA && !Character.isWhitespace(headerChars[i])) {
                        if (headerChars[i] == QUOTE) {
                            valueStart = i + 1;
                            searchingFor = SearchingFor.LAST_QUOTE;
                            weak = false;
                            malformed = false;
                        } else if (headerChars[i] == W) {
                            searchingFor = SearchingFor.WEAK_SLASH;
                        }
                    }
                    break;
                case WEAK_SLASH:
                    if (headerChars[i] == QUOTE) {
                        valueStart = i + 1;
                        searchingFor = SearchingFor.LAST_QUOTE;
                        weak = true;
                        malformed = false;
                    } else if (headerChars[i] != SLASH) {
                        return null; //malformed
                    }
                    break;
                case LAST_QUOTE:
                    if (headerChars[i] == QUOTE) {
                        String value = String.valueOf(headerChars, valueStart, i - valueStart);
                        return new ETag(weak, value.trim());
                    }
                    break;
                case END_OF_VALUE:
                    if (headerChars[i] == COMMA || Character.isWhitespace(headerChars[i])) {
                        if (!malformed) {
                            String value = String.valueOf(headerChars, valueStart, i - valueStart);
                            return new ETag(weak, value.trim());
                        }
                    }
                    break;
            }
        }
        if (searchingFor == SearchingFor.END_OF_VALUE || searchingFor == SearchingFor.LAST_QUOTE) {
            if (!malformed) {
                // Special case where we reached the end of the array containing the header values.
                String value = String.valueOf(headerChars, valueStart, headerChars.length - valueStart);
                return new ETag(weak, value.trim());
            }
        }

        return null;
    }

    enum SearchingFor {
        START_OF_VALUE, LAST_QUOTE, END_OF_VALUE, WEAK_SLASH;
    }
}
