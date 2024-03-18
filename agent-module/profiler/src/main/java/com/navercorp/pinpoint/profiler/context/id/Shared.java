/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.id;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface Shared {

    void maskErrorCode(int errorCode);

    int getErrorCode();

    void setLoggingInfo(byte loggingInfo);

    byte getLoggingInfo();

    void setEndPoint(String endPoint);

    String getEndPoint();


    void setRpcName(String rpc);

    String getRpcName();


    void setThreadId(long threadId);

    long getThreadId();

    void setStatusCode(int statusCode);

    int getStatusCode();

    boolean setUriTemplate(String uriTemplate);

    boolean setUriTemplate(String uriTemplate, boolean force);

    String getUriTemplate();

    boolean setHttpMethods(String httpMethod);

    String getHttpMethod();

    int incrementAndGetSqlCount();
}
