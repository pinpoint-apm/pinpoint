package com.navercorp.pinpoint.web.service;

import java.util.List;

import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.vo.BusinessTransactions;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.TransactionId;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;

/**
 *
 */
public interface TransactionInfoService {
	RecordSet createRecordSet(List<SpanAlign> spanAligns, long focusTimestamp);

	BusinessTransactions selectBusinessTransactions(List<TransactionId> traceIds, String applicationName, Range range, Filter filter);
}
