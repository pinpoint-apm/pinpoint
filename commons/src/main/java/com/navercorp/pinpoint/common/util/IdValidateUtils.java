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
import com.navercorp.pinpoint.common.id.AgentId;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author emeroad
 */
public final class IdValidateUtils {

    private static final int DEFAULT_MAX_LENGTH = PinpointConstants.AGENT_ID_MAX_LEN;

    public static String STABLE_VERSION_PATTERN_VALUE = "[0-9]+\\.[0-9]+\\.[0-9](-p[0-9]+)?";

    //    private static final Pattern ID_PATTERN = Pattern.compile("[a-zA-Z0-9\\._\\-]{1,24}");
    public static final String ID_PATTERN_VALUE = "[a-zA-Z0-9\\._\\-]+";
    private static final Pattern ID_PATTERN = Pattern.compile(ID_PATTERN_VALUE);

    private IdValidateUtils() {
    }

    public static boolean validateId(String id) {
        return validateId(id, DEFAULT_MAX_LENGTH);
    }

    public static boolean validateId(AgentId agentId) {
        return validateId(AgentId.unwrap(agentId));
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
        final Matcher matcher = ID_PATTERN.matcher(id);
        return matcher.matches();
    }

    public static boolean checkLength(String id, int maxLength) {
        Objects.requireNonNull(id, "id");

        final int idLength = id.length();
        if (idLength <= 0) {
            return false;
        }
        return idLength <= maxLength;
    }

    public static boolean checkId(String id, int offset, int length) {
        Matcher matcher = ID_PATTERN.matcher(id);
        matcher.region(offset, length);
        return matcher.matches();
    }

}
