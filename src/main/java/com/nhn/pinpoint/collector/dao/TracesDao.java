package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TSpanEvent;
import com.nhn.pinpoint.thrift.dto.TSpan;
import com.nhn.pinpoint.thrift.dto.TSpanChunk;

public interface TracesDao {
    void insert(TSpan span);

    void insertSpanChunk(TSpanChunk spanChunk);
}
