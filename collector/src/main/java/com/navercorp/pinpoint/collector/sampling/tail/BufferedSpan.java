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

package com.navercorp.pinpoint.collector.sampling.tail;

/**
 * Transient envelope used only to serialize a span/chunk into the Redis buffer and rebuild it at flush time.
 * Instances are never compared or hashed, so the record's default array-reference equals/hashCode are not used.
 */
public record BufferedSpan(
        Type type,
        String agentId,
        String agentName,
        String applicationName,
        long agentStartTime,
        long requestTime,
        byte[] protoBytes) {

    public enum Type {
        SPAN, SPAN_CHUNK
    }
}
