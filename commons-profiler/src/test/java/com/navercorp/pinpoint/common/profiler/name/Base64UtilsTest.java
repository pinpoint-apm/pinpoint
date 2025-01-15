/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.profiler.name;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * @author HyunGil Jeong
 */
public class Base64UtilsTest {

    @RepeatedTest(10)
    public void testEncodingAndDecoding() {
        UUID expected = UUID.randomUUID();
        String encoded = Base64Utils.encode(expected);
        Assertions.assertTrue(IdValidateUtils.validateId(encoded, PinpointConstants.AGENT_ID_MAX_LEN));
        UUID actual = Base64Utils.decode(encoded);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void decodeShouldFailWhenSrcIsNot22CharactersLong() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String invalid = "012345678901234567890";
            Base64Utils.decode(invalid);
        });
    }

    @Test
    public void decodeShouldFailWhenSrcContainsInvalidCharacter() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String invalid = "012345678901.345678901";
            Base64Utils.decode(invalid);
        });
    }

    @Test
    public void encodeStringShouldThrowNpeForNullArgument() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            Base64Utils.encode((String) null);
        });
    }

    @Test
    public void encodeStringShouldThrowIllegalArgumentExceptionForInvalidUuidString() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String invalidUuidString = "abcdefg";
            Base64Utils.encode(invalidUuidString);
        });
    }

    @Test
    public void encodeUuidShouldThrowNpeForNullArgument() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            Base64Utils.encode((UUID) null);
        });
    }

    @Test
    public void decodeShouldThrowNpeForNullArgument() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            Base64Utils.decode(null);
        });
    }
}
