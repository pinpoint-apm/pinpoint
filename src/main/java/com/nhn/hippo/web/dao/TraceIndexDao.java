package com.nhn.hippo.web.dao;

import com.nhn.hippo.web.vo.TraceId;

import java.util.List;

/**
 *
 */
public interface TraceIndexDao {
    List<List<TraceId>> scanTraceIndex(String agent, long start, long end);

    List<List<List<TraceId>>> multiScanTraceIndex(String[] agents, long start, long end);
}
