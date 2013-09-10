package com.nhn.pinpoint.web.dao;

import com.nhn.pinpoint.web.vo.TransactionId;

import java.util.List;

/**
 *
 */
@Deprecated
public interface TraceIndexDao {
    List<List<TransactionId>> scanTraceIndex(String agent, long start, long end);

//    List<List<List<TransactionId>>> multiScanTraceIndex(String[] agents, long start, long end);
}
