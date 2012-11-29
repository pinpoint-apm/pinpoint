package com.nhn.hippo.web.dao;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.nhn.hippo.web.vo.TraceId;
import com.profiler.common.bo.SpanBo;

/**
 *
 */
public interface TraceDao {

    List<SpanBo> selectSpan(UUID traceId);

    List<SpanBo> selectSpanAndAnnotation(UUID traceId);

    List<SpanBo> selectSpan(long traceIdMost, long traceIdLeast);
    
    List<List<SpanBo>> selectSpans(List<UUID> traceIds);

    List<List<SpanBo>> selectSpans(Set<TraceId> traceIds);

    List<SpanBo> selectSpans(TraceId traceId);
    
    @Deprecated
    List<List<SpanBo>> selectSpansAndAnnotation(Set<TraceId> traceIds);
}
