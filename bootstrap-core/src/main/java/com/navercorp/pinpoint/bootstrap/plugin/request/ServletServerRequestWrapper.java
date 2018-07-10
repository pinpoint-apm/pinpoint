/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.plugin.RequestWrapper;

/**
 * @author jaehong.kim
 */
public class ServletServerRequestWrapper implements ServerRequestWrapper {
    private final String rpcName;
    private final String endPoint;
    private final String remoteAddress;
    private final String acceptorHost;
    private final String method;
    private final String parameters;
    private final RequestWrapper requestWrapper;

    public ServletServerRequestWrapper(final RequestWrapper requestWrapper, final String rpcName, final String endPoint, final String remoteAddr, final String acceptorHost, final String method, final String parameters) {
        this.requestWrapper = requestWrapper;
        this.rpcName = rpcName;
        this.endPoint = endPoint;
        this.remoteAddress = remoteAddr;
        this.acceptorHost = acceptorHost;
        this.method = method;
        this.parameters = parameters;
    }

    @Override
    public String getHeader(String name) {
        return this.requestWrapper.getHeader(name);
    }

    @Override
    public String getRpcName() {
        return this.rpcName;
    }

    @Override
    public String getEndPoint() {
        return this.endPoint;
    }

    @Override
    public String getRemoteAddress() {
        return this.remoteAddress;
    }

    @Override
    public String getAcceptorHost() {
        return this.acceptorHost;
    }

    public String getMethod() {
        return this.method;
    }

    public String getParameters() {
        return this.parameters;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServletServerRequestWrapper{");
        sb.append("rpcName='").append(rpcName).append('\'');
        sb.append(", endPoint='").append(endPoint).append('\'');
        sb.append(", remoteAddress='").append(remoteAddress).append('\'');
        sb.append(", acceptorHost='").append(acceptorHost).append('\'');
        sb.append(", method='").append(method).append('\'');
        sb.append(", parameters='").append(parameters).append('\'');
        sb.append('}');
        return sb.toString();
    }
}