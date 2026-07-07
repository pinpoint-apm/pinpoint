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

package com.navercorp.pinpoint.collector.util;

/**
 * Deduplicates writes: each key is accepted at most once per dedup window.
 * <p>
 * Implementations may drop entries early (bounded capacity, expiry); callers
 * must tolerate an occasional duplicate acceptance. Include the time slot in
 * the key so that early expiry only costs a duplicate (idempotent) write,
 * never a missed one.
 */
public interface DedupCache<K> {

    /**
     * @return true if the key is seen for the first time and must be written
     */
    boolean update(K key);
}
