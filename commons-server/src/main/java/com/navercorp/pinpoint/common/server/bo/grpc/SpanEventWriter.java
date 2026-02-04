package com.navercorp.pinpoint.common.server.bo.grpc;

import com.navercorp.pinpoint.common.server.bo.SpanEventBo;

public interface SpanEventWriter {
    void write(SpanEventBo spanEvent);
}
