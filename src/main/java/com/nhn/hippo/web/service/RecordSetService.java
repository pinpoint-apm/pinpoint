package com.nhn.hippo.web.service;

import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.nhn.hippo.web.vo.callstacks.RecordSet;

import java.util.List;

/**
 *
 */
public interface RecordSetService {
    RecordSet createRecordSet(List<SpanAlign> spanAligns, long focusTimestamp);
}
