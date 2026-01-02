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

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.buffer.StringAllocator;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanDecodingContext {

    private final ServerTraceId transactionId;

    //    private AnnotationBo prevAnnotationBo;
    private long collectorAcceptedTime;

    private StringAllocator stringAllocator = StringAllocator.DEFAULT_ALLOCATOR;

    public SpanDecodingContext(ServerTraceId transactionId) {
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
    }

//    public AnnotationBo getPrevFirstAnnotationBo() {
//        return prevAnnotationBo;
//    }
//
//    public void setPrevFirstAnnotationBo(AnnotationBo prevAnnotationBo) {
//        this.prevAnnotationBo = prevAnnotationBo;
//    }

    public void setCollectorAcceptedTime(long collectorAcceptedTime) {
        this.collectorAcceptedTime = collectorAcceptedTime;
    }

    public long getCollectorAcceptedTime() {
        return collectorAcceptedTime;
    }

    public ServerTraceId getTransactionId() {
        return transactionId;
    }

    public String encoding(byte[] bytes) {
        return stringAllocator.allocate(bytes, 0, bytes.length, StandardCharsets.UTF_8);
    }

    public void setStringAllocator(StringAllocator stringAllocator) {
        this.stringAllocator = Objects.requireNonNull(stringAllocator, "stringAllocator");
    }

    public void next() {
    }

    public void finish() {
    }
}
