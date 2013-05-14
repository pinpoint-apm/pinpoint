package com.nhn.pinpoint.web.service;

import java.util.List;

import com.nhn.pinpoint.web.calltree.span.SpanAlign;
import com.nhn.pinpoint.web.vo.callstacks.RecordSet;

/**
 *
 */
public interface RecordSetService {
    RecordSet createRecordSet(List<SpanAlign> spanAligns, long focusTimestamp);
}
