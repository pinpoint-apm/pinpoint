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

package com.navercorp.pinpoint.web.trace.dao.mapper;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecodingContext;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanEncoder;

import java.util.Objects;
import java.util.function.Predicate;


/**
 * @author Taejin Koo
 */
public class FilteringSpanDecoder implements SpanDecoder {

    private final SpanDecoder delegate;
    private final Predicate<SpanBo> spanFilter;

    public FilteringSpanDecoder(SpanDecoder delegate, Predicate<SpanBo> spanFilter) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.spanFilter = Objects.requireNonNull(spanFilter, "spanFilter");
    }

    @Override
    public BasicSpan decode(Buffer qualifier, Buffer columnValue, SpanDecodingContext decodingContext) {
        if (!qualifier.hasRemaining()) {
            return null;
        }
        final byte type = qualifier.getByte(0);
        if (SpanEncoder.TYPE_SPAN == type) {
            BasicSpan spanBo = delegate.decode(qualifier, columnValue, decodingContext);
            if (spanBo != null ) {
                if (spanFilter.test((SpanBo) spanBo)) {
                    return spanBo;
                }
            }
            return null;
        }
        return null;
    }

    @Override
    public void next(SpanDecodingContext decodingContext) {
        delegate.next(decodingContext);
    }
}
