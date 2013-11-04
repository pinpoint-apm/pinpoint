package com.nhn.pinpoint.web.dao;


import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.common.bo.SpanBo;

/**
 * @author emeroad
 */
public interface TraceDao {

    List<SpanBo> selectSpan(TransactionId transactionId);

    List<SpanBo> selectSpanAndAnnotation(TransactionId transactionId);

    // TODO list하고 set하고 비교해서 하나 없애야 될듯 하다.
    List<List<SpanBo>> selectSpans(List<TransactionId> transactionIdList);
    
    List<List<SpanBo>> selectAllSpans(Collection<TransactionId> transactionIdList);

    List<List<SpanBo>> selectSpans(Set<TransactionId> transactionIdList);

    List<SpanBo> selectSpans(TransactionId transactionId);
    
    @Deprecated
    List<List<SpanBo>> selectSpansAndAnnotation(Set<TransactionId> transactionIdList);
}
