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
import org.junit.Assert;
import org.junit.Test;

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
            Assert.assertTrue(IdValidateUtils.validateId(encoded, PinpointConstants.AGENT_NAME_MAX_LEN));
            UUID actual = AgentUuidUtils.decode(encoded);
            Assert.assertEquals(expected, actual);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void decodeShouldFailWhenSrcIsNot22CharactersLong() {
        String invalid = "012345678901234567890";
        AgentUuidUtils.decode(invalid);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decodeShouldFailWhenSrcContainsInvalidCharacter() {
        String invalid = "012345678901.345678901";
        AgentUuidUtils.decode(invalid);
    }

    @Test(expected = NullPointerException.class)
    public void encodeStringShouldThrowNpeForNullArgument() {
        AgentUuidUtils.encode((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encodeStringShouldThrowIllegalArgumentExceptionForInvalidUuidString() {
        String invalidUuidString = "abcdefg";
        AgentUuidUtils.encode(invalidUuidString);
    }

    @Test(expected = NullPointerException.class)
    public void encodeUuidShouldThrowNpeForNullArgument() {
        AgentUuidUtils.encode((UUID) null);
    }

    @Test(expected = NullPointerException.class)
    public void decodeShouldThrowNpeForNullArgument() {
        AgentUuidUtils.decode(null);
    }
}
