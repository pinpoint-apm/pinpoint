/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.common.util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public final class StringUtils {

    private static final int DEFAULT_ABBREVIATE_MAX_WIDTH = 64;

    private static final String NULL_STRING = "null";

    private static final int INDEX_NOT_FOUND = -1;

    private StringUtils() {
    }

    public static String defaultString(final String str, final String defaultStr) {
        return str == null ? defaultStr : str;
    }

    public static boolean isEmpty(final String string) {
        return string == null || string.isEmpty();
    }

    public static boolean hasLength(final String string) {
        return string != null && string.length() > 0;
    }

    public static boolean hasText(String string) {
        if (isEmpty(string)) {
            return false;
        }

        final int length = string.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(string.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static <T> int getLength(final String string) {
        return getLength(string, 0);
    }

    public static <T> int getLength(final String string, final int nullValue) {
        if (string == null) {
            return nullValue;
        }
        return string.length();
    }

    public static String toString(final Object object) {
        if (object == null) {
            return NULL_STRING;
        }
        return object.toString();
    }


    public static String abbreviate(final String str) {
        return abbreviate(str, DEFAULT_ABBREVIATE_MAX_WIDTH);
    }


    public static String abbreviate(final String str, final int maxWidth) {
        if (str == null) {
            return NULL_STRING;
        }
        if (maxWidth < 0) {
            throw new IllegalArgumentException("negative maxWidth:" + maxWidth);
        }
        if (str.length() > maxWidth) {
            StringBuilder buffer = new StringBuilder(maxWidth + 10);
            buffer.append(str, 0, maxWidth);
            appendAbbreviateMessage(buffer, str.length());
            return buffer.toString();
        } else {
            return str;
        }
    }

    public static void appendAbbreviate(final StringBuilder builder, final String str, final int maxWidth) {
        if (str == null) {
            return;
        }
        if (maxWidth < 0) {
            return;
        }
        if (str.length() > maxWidth) {
            builder.append(str, 0, maxWidth);
            appendAbbreviateMessage(builder, str.length());
        } else {
            builder.append(str);
        }
    }

    private static void appendAbbreviateMessage(final StringBuilder buffer, final int strLength) {
        buffer.append("...(");
        buffer.append(strLength);
        buffer.append(')');
    }


    /**
     * Copy Spring Framework StringUtils
     *
     * <p>Tokenize the given String into a String array via a StringTokenizer.
     * Trims tokens and omits empty tokens.
     * <p>The given delimiters string is supposed to consist of any number of
     * delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using {@code delimitedListToStringArray}
     * @param str the String to tokenize
     * @param delimiters the delimiter characters, assembled as String
     * (each of those characters is individually considered as delimiter).
     * @return List of the tokens
     * @see java.util.StringTokenizer
     * @see String#trim()
     */
    public static List<String> tokenizeToStringList(final String str, final String delimiters) {
        return tokenizeToStringList(str, delimiters, true, true);
    }


    /**
     * Spring Framework StringUtils
     *
     * <p>Tokenize the given String into a String array via a StringTokenizer.
     * Trims tokens and omits empty tokens.
     * <p>The given delimiters string is supposed to consist of any number of
     * delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using {@code delimitedListToStringArray}
     * @param str the String to tokenize
     * @param delimiters the delimiter characters, assembled as String
     * (each of those characters is individually considered as delimiter).
     * @return List of the tokens
     * @see java.util.StringTokenizer
     * @see String#trim()
     */
    public static List<String> tokenizeToStringList(final String str, final String delimiters, final boolean trimTokens, final boolean ignoreEmptyTokens) {

        if (isEmpty(str)) {
            return Collections.emptyList();
        }
        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!ignoreEmptyTokens || token.length() > 0) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    /**
     * Apache Commons Lang StringUtils
     *
     * <p>Replaces all occurrences of a String within another String.</p>
     *
     * <p>A {@code null} reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringUtils.replace(null, *, *)        = null
     * StringUtils.replace("", *, *)          = ""
     * StringUtils.replace("any", null, *)    = "any"
     * StringUtils.replace("any", *, null)    = "any"
     * StringUtils.replace("any", "", *)      = "any"
     * StringUtils.replace("aba", "a", null)  = "aba"
     * StringUtils.replace("aba", "a", "")    = "b"
     * StringUtils.replace("aba", "a", "z")   = "zbz"
     * </pre>
     *
     * @see #replace(String text, String searchString, String replacement, int max)
     * @param text  text to search and replace in, may be null
     * @param searchString  the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @return the text with any replacements processed,
     *  {@code null} if null String input
     */
    public static String replace(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, -1);
    }

    /**
     * Apache Commons Lang StringUtils
     *
     * <p>Replaces a String with another String inside a larger String,
     * for the first {@code max} values of the search String.</p>
     *
     * <p>A {@code null} reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringUtils.replace(null, *, *, *)         = null
     * StringUtils.replace("", *, *, *)           = ""
     * StringUtils.replace("any", null, *, *)     = "any"
     * StringUtils.replace("any", *, null, *)     = "any"
     * StringUtils.replace("any", "", *, *)       = "any"
     * StringUtils.replace("any", *, *, 0)        = "any"
     * StringUtils.replace("abaa", "a", null, -1) = "abaa"
     * StringUtils.replace("abaa", "a", "", -1)   = "b"
     * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
     * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
     * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
     * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
     * </pre>
     *
     * @param text  text to search and replace in, may be null
     * @param searchString  the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @param max  maximum number of values to replace, or {@code -1} if no maximum
     * @return the text with any replacements processed,
     *  {@code null} if null String input
     */
    public static String replace(String text, String searchString, String replacement, int max) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        int start = 0;
        int end = text.indexOf(searchString, start);
        if (end == INDEX_NOT_FOUND) {
            return text;
        }
        int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = increase < 0 ? 0 : increase;
        increase *= max < 0 ? 16 : max > 64 ? 64 : max;
        StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != INDEX_NOT_FOUND) {
            buf.append(text.substring(start, end)).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = text.indexOf(searchString, start);
        }
        buf.append(text.substring(start));
        return buf.toString();
    }
}
