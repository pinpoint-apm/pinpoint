package com.nhn.pinpoint.web.service;

import java.util.List;

import com.nhn.pinpoint.web.calltree.span.SpanAlign;
import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.vo.BusinessTransactions;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.web.vo.callstacks.RecordSet;

/**
 *
 */
public interface TransactionInfoService {
	RecordSet createRecordSet(List<SpanAlign> spanAligns, long focusTimestamp);

	BusinessTransactions selectBusinessTransactions(List<TransactionId> traceIds, String applicationName, Range range, Filter filter);
}
