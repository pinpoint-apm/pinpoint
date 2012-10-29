package com.nhn.hippo.web.dao;

import java.util.List;

/**
 *
 */
public interface TraceIndexDao {
    List<byte[]> scanTraceIndex(String agent, long start, long end);

    List<List<byte[]>> multiScanTraceIndex(String[] agents, long start, long end);
}
