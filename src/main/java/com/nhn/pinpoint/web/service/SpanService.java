package com.nhn.pinpoint.web.service;

import java.util.List;

import com.nhn.pinpoint.web.calltree.span.SpanAlign;
import com.nhn.pinpoint.web.vo.TransactionId;

/**
 *
 */
public interface SpanService {
	List<SpanAlign> selectSpan(TransactionId traceId);
}
