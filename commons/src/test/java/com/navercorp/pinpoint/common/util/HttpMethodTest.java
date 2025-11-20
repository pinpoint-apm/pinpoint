/*
 * Copyright 2025 NAVER Corp.
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpMethodTest {

    /**
     * Test for the HttpMethod.valueOf method with known predefined methods.
     */
    @Test
    void testValueOfKnownMethod() {
        HttpMethod getMethod = HttpMethod.valueOf("GET");
        assertEquals(HttpMethod.GET, getMethod);

        HttpMethod postMethod = HttpMethod.valueOf("POST");
        assertEquals(HttpMethod.POST, postMethod);

        HttpMethod putMethod = HttpMethod.valueOf("PUT");
        assertEquals(HttpMethod.PUT, putMethod);

        HttpMethod deleteMethod = HttpMethod.valueOf("DELETE");
        assertEquals(HttpMethod.DELETE, deleteMethod);
    }

    /**
     * Test for HttpMethod.valueOf with unsupported method strings.
     */
    @Test
    void testValueOfUnknownMethod() {
        HttpMethod unknownMethod = HttpMethod.valueOf("UNKNOWN_METHOD");
        assertEquals(HttpMethod.UNKNOWN, unknownMethod);
    }

    /**
     * Test for HttpMethod.valueOf with null input, expecting a NullPointerException.
     */
    @Test
    void testValueOfNullInput() {
        assertThrows(NullPointerException.class, () -> HttpMethod.valueOf(null));
    }

    /**
     * Test for HttpMethod.valueOf with case-sensitive match.
     */
    @Test
    void testValueOfCaseSensitivity() {
        HttpMethod getMethod = HttpMethod.valueOf("GET");
        HttpMethod upperGetMethod = HttpMethod.valueOf("GET");
        HttpMethod lowerGetMethod = HttpMethod.valueOfIgnoreCase("get");
        assertEquals(getMethod, lowerGetMethod);
        assertEquals(getMethod, upperGetMethod);

    }
}