/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.context;

import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;

import java.util.TimerTask;

/**
 * @author emeroad
 */
public interface AsyncTrace {
    public static final int STATE_INIT = 0;
    public static final int STATE_FIRE = 1;
    public static final int STATE_TIMEOUT = 2;

    int getState();
    boolean fire();

    void setTimeoutTask(TimerTask timeoutTask);

    void setAsyncId(int asyncId);

    int getAsyncId();

    Object getAttachObject();

    void setAttachObject(Object attachObject);

    void traceBlockBegin();

    void markBeforeTime();

    long getBeforeTime();

    void traceBlockEnd();

    void markAfterTime();

    void recordApi(MethodDescriptor methodDescriptor);

    void recordException(Object result);

    void recordAttribute(AnnotationKey key, String value);

    void recordAttribute(AnnotationKey key, int value);

    void recordAttribute(AnnotationKey key, Object value);

    void recordServiceType(ServiceType serviceType);

    void recordRpcName(String rpcName);

    void recordDestinationId(String destinationId);

    // TODO: final String... an aggregated input needed so we don't have to sum up endPoints
    void recordEndPoint(String endPoint);
}
