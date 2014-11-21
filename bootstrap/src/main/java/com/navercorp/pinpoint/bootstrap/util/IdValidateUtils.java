package com.nhn.pinpoint.bootstrap.util;

import com.nhn.pinpoint.common.PinpointConstants;
import com.nhn.pinpoint.common.util.BytesUtils;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author emeroad
 */
public class IdValidateUtils {

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
