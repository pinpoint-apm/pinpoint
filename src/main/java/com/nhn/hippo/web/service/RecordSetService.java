package com.nhn.hippo.web.service;

import java.util.List;

import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.nhn.hippo.web.vo.callstacks.RecordSet;

/**
 *
 */
public interface RecordSetService {
    RecordSet createRecordSet(List<SpanAlign> spanAligns, long focusTimestamp);
}
