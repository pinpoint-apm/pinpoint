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

package com.navercorp.pinpoint.common.util;

import com.navercorp.pinpoint.common.PinpointConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * @author HyunGil Jeong
 */
public class AgentUuidUtilsTest {

    @Test
    public void testEncodingAndDecoding() {
        for (int i = 0; i < 1; ++i) {
            UUID expected = UUID.randomUUID();
            String encoded = AgentUuidUtils.encode(expected);
            Assertions.assertTrue(IdValidateUtils.validateId(encoded, PinpointConstants.AGENT_ID_MAX_LEN));
            UUID actual = AgentUuidUtils.decode(encoded);
            Assertions.assertEquals(expected, actual);
        }
    }

    @Test
    public void decodeShouldFailWhenSrcIsNot22CharactersLong() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String invalid = "012345678901234567890";
            AgentUuidUtils.decode(invalid);
        });
    }

    @Test
    public void decodeShouldFailWhenSrcContainsInvalidCharacter() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String invalid = "012345678901.345678901";
            AgentUuidUtils.decode(invalid);
        });
    }

    @Test
    public void encodeStringShouldThrowNpeForNullArgument() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            AgentUuidUtils.encode((String) null);
        });
    }

    @Test
    public void encodeStringShouldThrowIllegalArgumentExceptionForInvalidUuidString() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String invalidUuidString = "abcdefg";
            AgentUuidUtils.encode(invalidUuidString);
        });
    }

    @Test
    public void encodeUuidShouldThrowNpeForNullArgument() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            AgentUuidUtils.encode((UUID) null);
        });
    }

    @Test
    public void decodeShouldThrowNpeForNullArgument() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            AgentUuidUtils.decode(null);
        });
    }
}
