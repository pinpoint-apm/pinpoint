package com.nhn.hippo.web.dao;

import java.util.List;

/**
 * root trace index가 필요 없을것 같음.
 */
@Deprecated
public interface RootTraceIndexDao {
    List<List<byte[]>> scanTraceIndex(String agent, long start, long end);

    List<List<List<byte[]>>> multiScanTraceIndex(String[] agents, long start, long end);

    List<List<List<byte[]>>> parallelScanTraceIndex(String[] agents, long start, long end);
}
