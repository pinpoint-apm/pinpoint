/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.ktor;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class KtorConstantsTest {

    @BeforeAll
    static void registerServiceTypes() {
        // registrations come from this plugin's own type-provider.yml
        KtorTestServiceTypes.register();
    }

    @Test
    void serviceTypesMatchShippedTypeRegistry() {
        assertEquals("KTOR", KtorConstants.KTOR.getName());
        assertEquals(1160, KtorConstants.KTOR.getCode());
        assertEquals("KTOR_INTERNAL", KtorConstants.KTOR_INTERNAL.getName());
        assertEquals(1161, KtorConstants.KTOR_INTERNAL.getCode());
        assertEquals("KTOR_CLIENT", KtorConstants.KTOR_CLIENT.getName());
        assertEquals(9068, KtorConstants.KTOR_CLIENT.getCode());
        assertEquals("KTOR_CLIENT_INTERNAL", KtorConstants.KTOR_CLIENT_INTERNAL.getName());
        assertEquals(9069, KtorConstants.KTOR_CLIENT_INTERNAL.getCode());
    }

    @Test
    void serviceTypesAreDistinct() {
        assertNotEquals(KtorConstants.KTOR, KtorConstants.KTOR_INTERNAL);
        assertNotEquals(KtorConstants.KTOR, KtorConstants.KTOR_CLIENT);
        assertNotEquals(KtorConstants.KTOR_CLIENT, KtorConstants.KTOR_CLIENT_INTERNAL);
        assertNotEquals(KtorConstants.KTOR.getCode(), KtorConstants.KTOR_INTERNAL.getCode());
        assertNotEquals(KtorConstants.KTOR.getCode(), KtorConstants.KTOR_CLIENT.getCode());
        assertNotEquals(KtorConstants.KTOR_CLIENT.getCode(), KtorConstants.KTOR_CLIENT_INTERNAL.getCode());
    }
}
