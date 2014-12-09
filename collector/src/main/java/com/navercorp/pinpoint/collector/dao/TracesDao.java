package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;

/**
 * @author emeroad
 */
public interface TracesDao {
    void insert(TSpan span);

    void insertSpanChunk(TSpanChunk spanChunk);
}
