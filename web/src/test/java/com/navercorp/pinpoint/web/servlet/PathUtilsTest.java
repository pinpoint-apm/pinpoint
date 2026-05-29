/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.web.servlet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathUtilsTest {

    @Test
    void matchesSegment_exact() {
        assertTrue(PathUtils.matchesSegment("/api", "/api"));
    }

    @Test
    void matchesSegment_followedBySlash() {
        assertTrue(PathUtils.matchesSegment("/api/foo", "/api"));
        assertTrue(PathUtils.matchesSegment("/assets/main.css", "/assets"));
    }

    @Test
    void matchesSegment_boundaryRejected() {
        // segment boundary check: '/api-public' is NOT a match for '/api'
        assertFalse(PathUtils.matchesSegment("/api-public", "/api"));
        assertFalse(PathUtils.matchesSegment("/api-public/x", "/api"));
        assertFalse(PathUtils.matchesSegment("/apination", "/api"));
    }

    @Test
    void matchesSegment_prefixMismatch() {
        assertFalse(PathUtils.matchesSegment("/v3/api", "/api"));
        assertFalse(PathUtils.matchesSegment("/", "/api"));
    }

    @Test
    void matchesSegment_emptyPath() {
        assertFalse(PathUtils.matchesSegment("", "/api"));
    }

    @Test
    void matchesSegment_withStartOffset() {
        // skip leading "//" and match from offset 1
        assertTrue(PathUtils.matchesSegment("//api/foo", "/api", 1));
        // offset puts us past the actual prefix → no match
        assertFalse(PathUtils.matchesSegment("/api/foo", "/api", 1));
    }

    @Test
    void matchesSegment_prefixMatch_relaxesBoundary() {
        // prefixMatch=true is equivalent to String.startsWith
        assertTrue(PathUtils.matchesSegment("/api-public", "/api", 0, true));
        assertTrue(PathUtils.matchesSegment("/api-public/x", "/api", 0, true));
        assertTrue(PathUtils.matchesSegment("/apination", "/api", 0, true));
        assertTrue(PathUtils.matchesSegment("/api", "/api", 0, true));
        assertTrue(PathUtils.matchesSegment("/api/foo", "/api", 0, true));
    }

    @Test
    void matchesSegment_prefixMatch_stillRequiresPrefix() {
        // prefixMatch=true does not bypass the prefix requirement itself
        assertFalse(PathUtils.matchesSegment("/v3/api", "/api", 0, true));
        assertFalse(PathUtils.matchesSegment("/", "/api", 0, true));
    }
}
