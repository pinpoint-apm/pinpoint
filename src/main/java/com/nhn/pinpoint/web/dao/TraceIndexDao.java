package com.nhn.pinpoint.web.dao;

import com.nhn.pinpoint.web.vo.TraceId;

import java.util.List;

/**
 *
 */
@Deprecated
public interface TraceIndexDao {
    List<List<TraceId>> scanTraceIndex(String agent, long start, long end);

//    List<List<List<TraceId>>> multiScanTraceIndex(String[] agents, long start, long end);
}
