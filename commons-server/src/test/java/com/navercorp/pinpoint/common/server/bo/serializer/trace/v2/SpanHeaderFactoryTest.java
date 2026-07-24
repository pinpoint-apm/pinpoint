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

import com.navercorp.pinpoint.common.server.bo.TraceSourceType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpanHeaderFactoryTest {

    private final SpanHeaderFactory factory = new SpanHeaderFactory(false);
    private final SpanHeaderFactory uidFactory = new SpanHeaderFactory(true);

    @Test
    void span_factory() {
        assertThat(factory.span(TraceSourceType.PINPOINT)).isSameAs(SpanHeader.SPAN);
        assertThat(factory.span(TraceSourceType.OPENTELEMETRY)).isSameAs(SpanHeader.OTEL_SPAN);
        assertThat(factory.spanChunk(TraceSourceType.PINPOINT)).isSameAs(SpanHeader.SPAN_CHUNK);
        assertThat(factory.spanChunk(TraceSourceType.OPENTELEMETRY)).isSameAs(SpanHeader.OTEL_SPAN_CHUNK);
    }

    @Test
    void span_factory_serviceUid() {
        assertThat(uidFactory.span(TraceSourceType.PINPOINT)).isSameAs(SpanHeader.SPAN_UID);
        assertThat(uidFactory.span(TraceSourceType.OPENTELEMETRY)).isSameAs(SpanHeader.OTEL_SPAN_UID);
        assertThat(uidFactory.spanChunk(TraceSourceType.PINPOINT)).isSameAs(SpanHeader.SPAN_CHUNK_UID);
        assertThat(uidFactory.spanChunk(TraceSourceType.OPENTELEMETRY)).isSameAs(SpanHeader.OTEL_SPAN_CHUNK_UID);
    }
}
