package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.vo.TransactionId;

/**
 * @author emeroad
 */
public interface SpanService {
	SpanResult selectSpan(TransactionId transactionId, long selectedSpanHint);
}
