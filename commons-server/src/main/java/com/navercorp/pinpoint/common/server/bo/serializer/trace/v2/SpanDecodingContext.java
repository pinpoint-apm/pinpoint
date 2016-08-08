package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.util.TransactionId;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanDecodingContext {

//    private AnnotationBo prevAnnotationBo;
    private long collectorAcceptedTime;
    private TransactionId transactionId;

//    public AnnotationBo getPrevFirstAnnotationBo() {
//        return prevAnnotationBo;
//    }
//
//    public void setPrevFirstAnnotationBo(AnnotationBo prevAnnotationBo) {
//        this.prevAnnotationBo = prevAnnotationBo;
//    }


    public void setCollectorAcceptedTime(long collectorAcceptedTime) {
        this.collectorAcceptedTime = collectorAcceptedTime;
    }

    public long getCollectorAcceptedTime() {
        return collectorAcceptedTime;
    }

    public void setTransactionId(TransactionId transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionId getTransactionId() {
        return transactionId;
    }


    public void next() {
    }

    public void finish() {
    }
}
