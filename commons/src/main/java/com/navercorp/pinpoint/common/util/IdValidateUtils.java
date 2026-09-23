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

    @Deprecated
    public static String STABLE_VERSION_PATTERN_VALUE = AgentVersionPostfix.STABLE_VERSION_PATTERN_STRING;

    /**
     * Kept for error messages. {@link #checkPattern(String)} checks the same character set without a regex,
     * because it runs on the collector for every span and header.
     */
    public static final String ID_PATTERN_VALUE = "[a-zA-Z0-9._\\-]+";

    private IdValidateUtils() {
    }

    public static boolean validateId(String id) {
        return validateId(id, DEFAULT_MAX_LENGTH);
    }

    public static boolean validateId(String id, int maxLength) {
        final CheckResult result = checkId(id, maxLength);
        return result == CheckResult.SUCCESS;
    }

    public enum CheckResult {
        SUCCESS,
        FAIL_LENGTH,
        FAIL_PATTERN;
    }

    public static CheckResult checkId(String id, int maxLength) {
        Objects.requireNonNull(id, "id");

        if (maxLength <= 0) {
            throw new IllegalArgumentException("negative maxLength:" + maxLength);
        }

        if (!checkLength(id, maxLength)) {
            return CheckResult.FAIL_LENGTH;
        }
        if (!checkPattern(id)) {
            return CheckResult.FAIL_PATTERN;
        }
        return CheckResult.SUCCESS;
    }

    public static boolean checkPattern(String id) {
        return checkId(id, 0, id.length());
    }

    public static boolean checkLength(String id, int maxLength) {
        Objects.requireNonNull(id, "id");

        final int idLength = id.length();
        if (idLength <= 0) {
            return false;
        }
        return idLength <= maxLength;
    }

    /**
     * @param start inclusive start index
     * @param end   exclusive end index
     * @return true when {@code id[start, end)} is non-empty and every char matches {@link #ID_PATTERN_VALUE}
     */
    public static boolean checkId(String id, int start, int end) {
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
