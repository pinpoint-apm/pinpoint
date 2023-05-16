/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecodingContext;

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
    public Object decode(Buffer qualifier, Buffer columnValue, SpanDecodingContext decodingContext) {
        final Object decodedObject = delegate.decode(qualifier, columnValue, decodingContext);

        if (decodedObject instanceof SpanBo) {
            final SpanBo spanBo = (SpanBo) decodedObject;
            if (spanFilter.test(spanBo)) {
                return spanBo;
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
