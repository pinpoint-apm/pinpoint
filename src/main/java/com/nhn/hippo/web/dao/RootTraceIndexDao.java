package com.nhn.hippo.web.dao;

import java.util.List;

/**
 *
 */
public interface RootTraceIndexDao {
    List<List<byte[]>> scanTraceIndex(String agent, long start, long end);

    List<List<List<byte[]>>> multiScanTraceIndex(String[] agents, long start, long end);

    List parallelScanTraceIndex(String[] agents, long start, long end);

}
