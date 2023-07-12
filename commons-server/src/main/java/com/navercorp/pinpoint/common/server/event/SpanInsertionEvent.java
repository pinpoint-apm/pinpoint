package com.navercorp.pinpoint.common.server.event;

import com.navercorp.pinpoint.common.server.bo.SpanBo;

public class SpanInsertionEvent {
    private final SpanBo spanBo;
    private final boolean success;

    public SpanInsertionEvent(SpanBo spanBo, boolean success) {
        this.spanBo = spanBo;
        this.success = success;
    }

    public SpanBo getSpanBo() {
        return spanBo;
    }

    public boolean isSuccess() {
        return success;
    }
}
