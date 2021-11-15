package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface TraceDao {
    void insert(SpanBo span);

    void insertSpanChunk(SpanChunkBo spanChunk);
}
