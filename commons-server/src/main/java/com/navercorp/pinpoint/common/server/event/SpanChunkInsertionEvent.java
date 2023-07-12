package com.navercorp.pinpoint.common.server.event;

import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;

public class SpanChunkInsertionEvent {
    private final SpanChunkBo spanChunkBo;
    private final boolean success;

    public SpanChunkInsertionEvent(SpanChunkBo spanChunkBo, boolean success) {
        this.spanChunkBo = spanChunkBo;
        this.success = success;
    }

    public SpanChunkBo getSpanChunkBo() {
        return spanChunkBo;
    }

    public boolean isSuccess() {
        return success;
    }
}
