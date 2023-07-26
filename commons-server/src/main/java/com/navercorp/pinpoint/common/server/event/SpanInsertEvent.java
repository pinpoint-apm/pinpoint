package com.navercorp.pinpoint.common.server.event;

import com.navercorp.pinpoint.common.server.bo.SpanBo;

import java.util.Objects;

public class SpanInsertEvent {
    private final SpanBo spanBo;
    private final boolean success;

    public SpanInsertEvent(SpanBo spanBo, boolean success) {
        this.spanBo = Objects.requireNonNull(spanBo, "spanBo");
        this.success = success;
    }

    public SpanBo getSpanBo() {
        return spanBo;
    }

    public boolean isSuccess() {
        return success;
    }
}
