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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

/**
 * Accumulates the number of leaf values truncated across a single attribute-tree transform pass
 * (including nested ARRAY / KVLIST values). Create one instance per pass — truncation totals are
 * reported per category (attributes / sql / events / links), so a counter must not be reused
 * across categories or spans. Not thread-safe.
 */
public final class TruncationCounter {
    private int truncatedCount;

    public void truncated() {
        truncatedCount++;
    }

    public int truncatedCount() {
        return truncatedCount;
    }
}