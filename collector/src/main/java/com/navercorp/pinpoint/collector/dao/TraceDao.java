package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;

import java.util.concurrent.CompletableFuture;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface TraceDao {

    boolean insert(SpanBo span);

    CompletableFuture<Void> asyncInsert(SpanBo span);

    void insertSpanChunk(SpanChunkBo spanChunk);
}
