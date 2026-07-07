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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface BasicSpan {

    int getVersion();

    @NonNull
    TraceSourceType getTraceSourceType();

    SpanOwner getSpanOwner();
    void setSpanOwner(SpanOwner owner);

    String getAgentId();

    String getAgentName();

    String getApplicationName();

    String getServiceName();

    ServiceUid getServiceUid();

    long getAgentStartTime();

    long getSpanId();
    void setSpanId(long spanId);

    ServerTraceId getTransactionId();
//    void setTransactionId(TransactionId transactionId);

    int getApplicationServiceType();
    void setApplicationServiceType(int applicationServiceType);
    boolean hasApplicationServiceType();

    long getCollectorAcceptTime();
    void setCollectorAcceptTime(long collectorAcceptTime);

//    List<SpanEventBo> getSpanEventBoList();
}
