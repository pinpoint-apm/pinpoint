package com.navercorp.pinpoint.common.server.event;

import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;

import java.util.Objects;

public class SpanChunkInsertEvent {
    private final SpanChunkBo spanChunkBo;
    private final boolean success;

    public SpanChunkInsertEvent(SpanChunkBo spanChunkBo, boolean success) {
        this.spanChunkBo = Objects.requireNonNull(spanChunkBo, "spanChunkBo");
        this.success = success;
    }

    public SpanChunkBo getSpanChunkBo() {
        return spanChunkBo;
    }

    public boolean isSuccess() {
        return success;
    }
}
