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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.common.ServiceType;

/**
 * @author emeroad
 */
public interface StackFrame {

    int getStackFrameId();

    void setStackFrameId(int stackId);

    void markBeforeTime();

    long getBeforeTime();

    void markAfterTime();

    long getAfterTime();

    int getElapsedTime();

    void setEndPoint(String endPoint);

    void setRpc(String rpc);

    void setApiId(int apiId);

    void setExceptionInfo(int exceptionId, String exceptionMessage);

    void setServiceType(short serviceType);

    void addAnnotation(Annotation annotation);
    
    ServiceType getServiceType();

    void attachFrameObject(Object frameObject);

    Object getFrameObject();

    Object detachFrameObject();
}
