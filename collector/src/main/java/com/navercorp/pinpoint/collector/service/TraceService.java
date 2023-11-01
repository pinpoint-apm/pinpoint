package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import jakarta.validation.Valid;

public interface TraceService {
    void insertSpanChunk(@Valid SpanChunkBo spanChunkBo);

    void insertSpan(@Valid SpanBo spanBo);
}
