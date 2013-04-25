package com.nhn.hippo.web.service;

import java.util.List;

import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.nhn.hippo.web.vo.TraceId;

/**
 *
 */
public interface SpanService {
	List<SpanAlign> selectSpan(TraceId traceId);
}
