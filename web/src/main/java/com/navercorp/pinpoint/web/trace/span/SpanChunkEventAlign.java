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

package com.navercorp.pinpoint.web.trace.span;

import com.navercorp.pinpoint.common.server.bo.LocalAsyncIdBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.io.SpanVersion;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class SpanChunkEventAlign extends SpanEventAlign {

    private final SpanChunkBo spanChunkBo;

    public SpanChunkEventAlign(SpanBo spanBo, SpanChunkBo spanChunkBo, SpanEventBo spanEventBo) {
        this(spanBo, spanChunkBo, spanEventBo, false);
    }

    public SpanChunkEventAlign(SpanBo spanBo, SpanChunkBo spanChunkBo, SpanEventBo spanEventBo, boolean opentelemetry) {
        super(spanBo, spanEventBo, opentelemetry);
        this.spanChunkBo = Objects.requireNonNull(spanChunkBo, "spanChunkBo");
    }

    @Override
    public boolean isAsync() {
        return spanChunkBo.isAsyncSpanChunk();
    }

    @Override
    public long getStartTimeNanos() {
        final int version = spanChunkBo.getVersion();
        if (version == SpanVersion.TRACE_V1) {
            return super.getStartTimeNanos();
        } else if (version == SpanVersion.TRACE_V2) {
            if (isOpenTelemetry()) {
                return spanChunkBo.getKeyTimeNanos();
            }
            return spanChunkBo.getKeyTimeNanos() + TimeUnit.MILLISECONDS.toNanos(getSpanEventBo().getStartElapsed());
        } else if (version == SpanVersion.TRACE_V3) {
            if (isOpenTelemetry() && getSpanEventBo().hasStartTime()) {
                return getSpanEventBo().getStartTimeNanos();
            }
            return spanChunkBo.getKeyTimeNanos() + TimeUnit.MILLISECONDS.toNanos(getSpanEventBo().getStartElapsed());
        } else {
            throw new IllegalStateException("unsupported version:" + version);
        }
    }

    @Override
    public boolean isAsyncFirst() {
        if (!spanChunkBo.isAsyncSpanChunk()) {
            return false;
        }
        return getSpanEventBo().getSequence() == 0;
    }

    @Override
    public int getAsyncId() {
        final LocalAsyncIdBo localAsyncIdBo = spanChunkBo.getLocalAsyncId();
        if (localAsyncIdBo == null) {
            return -1;
        }
        return localAsyncIdBo.getAsyncId();
    }
}
