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
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.vo.GetTraceInfo;
import com.navercorp.pinpoint.web.vo.SpanHint;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class TargetSpanDecoder implements SpanDecoder {

    private final SpanDecoder delegate;
    private final GetTraceInfo targetTraceInfo;

    public TargetSpanDecoder(SpanDecoder realSpanDecoder, GetTraceInfo targetTraceInfo) {
        this.delegate = Objects.requireNonNull(realSpanDecoder, "realSpanDecoder");

        Objects.requireNonNull(targetTraceInfo, "targetTraceInfo");

        SpanHint hint = targetTraceInfo.getHint();
        Assert.isTrue(hint.isSet(), "hint must be set");
        this.targetTraceInfo = Objects.requireNonNull(targetTraceInfo, "targetTraceInfo");
    }

    @Override
    public Object decode(Buffer qualifier, Buffer columnValue, SpanDecodingContext decodingContext) {
        final Object decodedObject = delegate.decode(qualifier, columnValue, decodingContext);

        if (decodedObject instanceof SpanBo) {
            final SpanBo spanBo = (SpanBo) decodedObject;

            final TransactionId transactionId = spanBo.getTransactionId();

            final TransactionId expectedTransactionId = targetTraceInfo.getTransactionId();
            if (!expectedTransactionId.equals(transactionId)) {
                return null;
            }

            final SpanHint hint = targetTraceInfo.getHint();
            final long collectorAcceptTime = spanBo.getCollectorAcceptTime();
            if (collectorAcceptTime != hint.getCollectorAcceptorTime()) {
                return null;
            }

            final int elapsed = spanBo.getElapsed();
            if (elapsed != hint.getResponseTime()) {
                return null;
            }

            return spanBo;
        }

        return null;
    }

    @Override
    public void next(SpanDecodingContext decodingContext) {
        delegate.next(decodingContext);
    }
}
