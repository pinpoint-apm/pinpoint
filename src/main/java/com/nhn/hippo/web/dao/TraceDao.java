package com.nhn.hippo.web.dao;


import com.nhn.hippo.web.vo.TraceId;
import com.profiler.common.dto.thrift.Span;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public interface TraceDao {

    List<Span> selectSpan(UUID traceId);

    List<Span> selectSpan(long traceIdMost, long traceIdLeast);

    List<List<Span>> selectSpans(List<UUID> traceIds);

    List<List<Span>> selectSpans(Set<TraceId> traceIds);

}
