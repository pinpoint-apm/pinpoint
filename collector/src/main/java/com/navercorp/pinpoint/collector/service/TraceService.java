package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;

public interface TraceService {
    void insertSpanChunk(SpanChunkBo spanChunkBo);

    void insertSpan(SpanBo spanBo);
}
