package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface TraceDao {
    boolean insert(SpanBo span);

    boolean insertSpanChunk(SpanChunkBo spanChunk);
}
