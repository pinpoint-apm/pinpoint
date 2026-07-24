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

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Lossy, filter-only hashes embedded in the span qualifier so an HBase
 * QualifierFilter can pre-filter cells at the region server. Every value here is
 * a coarse bucket, never an identity: the exact field (e.g. the applicationName
 * string) still travels in the qualifier and stays authoritative for the final
 * client-side match.
 *
 * <p>These hashes are part of the stored qualifier layout, so the algorithm must
 * stay stable across versions. The write side (encoder) and the read side (query
 * filter builder) must call the same method to agree on the bucket.
 */
public final class SpanQualifierHash {

    // murmur3 avalanches into all bits, so the low byte is a uniform 256-bucket value;
    // this is the same function/input the scatter and agentId row keys hash applicationName
    // with (see TraceIndexRowKeyUtils/AgentIdRowKeyUtils). _fixed guarantees a stable algorithm.
    private static final HashFunction HASH = Hashing.murmur3_32_fixed();

    private SpanQualifierHash() {
    }

    /**
     * 1-byte (256-bucket) hash of applicationName. Filtering runs within a single
     * trace row, where the number of distinct applications is small, so 256 buckets
     * make collisions between co-resident applications rare; the exact applicationName
     * check remains the source of truth.
     */
    public static byte applicationName(String applicationName) {
        if (applicationName == null) {
            return 0;
        }
        return (byte) HASH.hashUnencodedChars(applicationName).asInt();
    }
}
