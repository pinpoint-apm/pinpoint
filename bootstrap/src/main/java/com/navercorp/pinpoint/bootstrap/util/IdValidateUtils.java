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

package com.navercorp.pinpoint.bootstrap.util;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author emeroad
 */
public final class IdValidateUtils {

    private static final int DEFAULT_MAX_LENGTH = PinpointConstants.AGENT_NAME_MAX_LEN;

//    private static final Pattern ID_PATTERN = Pattern.compile("[a-zA-Z0-9\\._\\-]{1,24}");
    private static final Pattern ID_PATTERN = Pattern.compile("[a-zA-Z0-9\\._\\-]+");

    private IdValidateUtils() {
    }

    public static boolean validateId(String id) {
        return validateId(id, DEFAULT_MAX_LENGTH);
    }

    public static boolean validateId(String id, int maxLength) {
        if (id == null) {
            throw new NullPointerException("id must not be null");
        }
        if (maxLength <= 0) {
            throw new IllegalArgumentException("negative maxLength:" + maxLength);
        }

        final Matcher matcher = ID_PATTERN.matcher(id);
        if (matcher.matches()) {
            return checkBytesLength(id, maxLength);
        } else {
            return false;
        }
    }

    private static boolean checkBytesLength(String id, int maxLength) {
        // try encode
        final byte[] idBytes = BytesUtils.toBytes(id);
        if (idBytes == null || idBytes.length == 0) {
            throw new IllegalArgumentException("toBytes fail. id:" + id);
        }
        return idBytes.length <= maxLength;
    }

}
