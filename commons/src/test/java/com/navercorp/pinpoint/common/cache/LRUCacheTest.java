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

package com.navercorp.pinpoint.common.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LRUCacheTest {

    @Test
    void put() {
        Cache<String, String> cache = new LRUCache<>(3);
        cache.put("a", "a");
        cache.put("b", "b");
        cache.put("c", "c");
        cache.put("d", "d");

        Assertions.assertNull(cache.get("a"));
    }

    @Test
    void put_accessOrder() {
        Cache<String, String> cache = new LRUCache<>(3);
        cache.put("a", "a");
        cache.put("b", "b");
        cache.put("c", "c");

        // access order
        cache.get("a");
        cache.put("d", "dd");

        Assertions.assertNull(cache.get("b"));
        Assertions.assertNotNull(cache.get("a"));
    }
}