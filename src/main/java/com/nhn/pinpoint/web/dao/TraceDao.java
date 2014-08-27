package com.nhn.pinpoint.web.dao;


import java.util.Collection;
import java.util.List;

import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.common.bo.SpanBo;

/**
 * @author emeroad
 */
public interface TraceDao {

    List<SpanBo> selectSpan(TransactionId transactionId);

    List<SpanBo> selectSpanAndAnnotation(TransactionId transactionId);

    List<List<SpanBo>> selectSpans(List<TransactionId> transactionIdList);
    
    List<List<SpanBo>> selectAllSpans(Collection<TransactionId> transactionIdList);

    List<SpanBo> selectSpans(TransactionId transactionId);
    
}
