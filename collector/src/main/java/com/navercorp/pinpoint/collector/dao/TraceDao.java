package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface TraceDao {
    void insert(TSpan span);

    void insertSpanChunk(TSpanChunk spanChunk);
}
