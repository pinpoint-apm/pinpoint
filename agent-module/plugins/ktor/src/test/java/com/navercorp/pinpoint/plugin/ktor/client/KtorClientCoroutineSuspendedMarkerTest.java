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

package com.navercorp.pinpoint.plugin.ktor.client;

import kotlin.coroutines.intrinsics.IntrinsicsKt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KtorClientCoroutineSuspendedMarkerTest {

    @Test
    void detectsOnlyTheKotlinCoroutineMarker() {
        Object marker = IntrinsicsKt.getCOROUTINE_SUSPENDED();

        assertTrue(KtorClientCoroutineSuspendedMarker.isSuspended(marker));

        assertFalse(KtorClientCoroutineSuspendedMarker.isSuspended(null));
        assertFalse(KtorClientCoroutineSuspendedMarker.isSuspended(new Object()));
        assertFalse(KtorClientCoroutineSuspendedMarker.isSuspended("COROUTINE_SUSPENDED"));
    }
}
