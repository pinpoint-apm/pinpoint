package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TSpan;
import com.nhn.pinpoint.thrift.dto.TSpanChunk;

/**
 * @author emeroad
 */
public interface TracesDao {
    void insert(TSpan span);

    void insertSpanChunk(TSpanChunk spanChunk);
}
