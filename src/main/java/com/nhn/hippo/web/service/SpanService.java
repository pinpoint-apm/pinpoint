package com.nhn.hippo.web.service;

import java.util.List;

import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.nhn.hippo.web.vo.RequestMetadataQuery;
import com.nhn.hippo.web.vo.TraceId;
import com.profiler.common.bo.SpanBo;

/**
 *
 */
public interface SpanService {
	List<SpanAlign> selectSpan(TraceId traceId);

	List<SpanBo> selectRequestMetadata(RequestMetadataQuery query);
}
