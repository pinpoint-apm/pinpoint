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

package com.navercorp.pinpoint.common.util;

import com.navercorp.pinpoint.common.PinpointConstants;

import java.util.Objects;

/**
 * @author emeroad
 */
public final class IdValidateUtils {

    private static final int DEFAULT_MAX_LENGTH = PinpointConstants.AGENT_ID_MAX_LEN;
    private static final int MIN_LENGTH = 1;

    @Deprecated
    public static String STABLE_VERSION_PATTERN_VALUE = AgentVersionPostfix.STABLE_VERSION_PATTERN_STRING;

    /**
     * Kept for error messages. {@link #checkIdPattern(String)} checks the same character set without a regex,
     * because it runs on the collector for every span and header.
     */
    public static final String ID_PATTERN_VALUE = "[a-zA-Z0-9._\\-]+";

    /**
     * Kept for error messages. {@link #checkUrlPattern(String)} checks the same character set without a regex.
     * The set is the RFC 3986 URI characters: unreserved, gen-delims, sub-delims and {@code %} for percent-encoding.
     */
    public static final String URL_PATTERN_VALUE = "[a-zA-Z0-9._\\-~:/?#\\[\\]@!$&'()*+,;=%]+";

    private IdValidateUtils() {
    }

    public static boolean validateId(String id) {
        return validateId(id, DEFAULT_MAX_LENGTH);
    }

    public static boolean validateId(String id, int maxLength) {
        final CheckResult result = checkId(id, maxLength);
        return result == CheckResult.SUCCESS;
    }

    public static boolean validateId(String id, int minLength, int maxLength) {
        final CheckResult result = checkId(id, minLength, maxLength);
        return result == CheckResult.SUCCESS;
    }

    public enum CheckResult {
        SUCCESS,
        FAIL_LENGTH,
        FAIL_PATTERN;
    }

    public static CheckResult checkId(String id, int maxLength) {
        return checkId(id, MIN_LENGTH, maxLength);
    }

    /**
     * @param minLength inclusive minimum length, at least 1
     * @param maxLength inclusive maximum length
     */
    public static CheckResult checkId(String id, int minLength, int maxLength) {
        Objects.requireNonNull(id, "id");

        if (minLength < MIN_LENGTH) {
            throw new IllegalArgumentException("negative minLength:" + minLength);
        }
        if (maxLength < minLength) {
            throw new IllegalArgumentException("maxLength:" + maxLength + " < minLength:" + minLength);
        }

        if (!checkLength(id, minLength, maxLength)) {
            return CheckResult.FAIL_LENGTH;
        }
        if (!checkIdPattern(id)) {
            return CheckResult.FAIL_PATTERN;
        }
        return CheckResult.SUCCESS;
    }

    public static boolean checkIdPattern(String id) {
        return checkIdPattern(id, 0, id.length());
    }

    public static boolean checkLength(String id, int maxLength) {
        return checkLength(id, MIN_LENGTH, maxLength);
    }

    public static boolean checkLength(String id, int minLength, int maxLength) {
        Objects.requireNonNull(id, "id");

        final int idLength = id.length();
        if (idLength < minLength) {
            return false;
        }
        return idLength <= maxLength;
    }

    /**
     * @param start inclusive start index
     * @param end   exclusive end index
     * @return true when {@code id[start, end)} is non-empty and every char matches {@link #ID_PATTERN_VALUE}
     */
    public static boolean checkIdPattern(String id, int start, int end) {
        if (start < 0 || end > id.length() || start > end) {
            throw new IndexOutOfBoundsException("start:" + start + " end:" + end + " length:" + id.length());
        }
        if (start == end) {
            return false;
        }
        for (int i = start; i < end; i++) {
            if (!isIdChar(id.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true when {@code url} is non-empty and every char matches {@link #URL_PATTERN_VALUE}
     */
    public static boolean checkUrlPattern(String url) {
        Objects.requireNonNull(url, "url");

        final int length = url.length();
        if (length == 0) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (!isUrlChar(url.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isUrlChar(char c) {
        if (isIdChar(c)) {
            return true;
        }
        switch (c) {
            // unreserved
            case '~':
            // gen-delims
            case ':':
            case '/':
            case '?':
            case '#':
            case '[':
            case ']':
            case '@':
            // sub-delims
            case '!':
            case '$':
            case '&':
            case '\'':
            case '(':
            case ')':
            case '*':
            case '+':
            case ',':
            case ';':
            case '=':
            // percent-encoding
            case '%':
                return true;
            default:
                return false;
        }
    }

    private static boolean isIdChar(char c) {
        if (c >= 'a' && c <= 'z') {
            return true;
        }
        if (c >= 'A' && c <= 'Z') {
            return true;
        }
        if (c >= '0' && c <= '9') {
            return true;
        }
        return c == '.' || c == '_' || c == '-';
    }

}
