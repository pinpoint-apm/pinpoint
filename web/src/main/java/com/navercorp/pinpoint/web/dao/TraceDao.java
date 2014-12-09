package com.navercorp.pinpoint.web.dao;


import java.util.Collection;
import java.util.List;

import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.web.vo.TransactionId;

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
