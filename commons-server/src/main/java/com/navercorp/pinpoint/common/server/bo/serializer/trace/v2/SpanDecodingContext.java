/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;

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
