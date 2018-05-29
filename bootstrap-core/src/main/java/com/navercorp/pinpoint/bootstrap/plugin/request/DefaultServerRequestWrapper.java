/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.request;

import java.util.Map;

/**
 * @author jaehong.kim
 */
public class DefaultServerRequestWrapper implements ServerRequestWrapper {
    private String rpcName;
    private String endPoint;
    private String remoteAddress;
    private String acceptorHost;
    private String method;
    private String parameters;
    private String parentApplicationName;
    private String host;
    private String type;
    private String samplingFlag;
    private String parentApplicationNamespace;
    private String transactionId;
    private long parentSpanId;
    private long spanId;
    private short flags;
    private Map<String, String> proxyHeaderMap;

    @Override
    public String getRpcName() {
        return rpcName;
    }

    public void setRpcName(String rpcName) {
        this.rpcName = rpcName;
    }

    @Override
    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    public String getAcceptorHost() {
        return this.acceptorHost;
    }

    public void setAcceptorHost(String acceptorHost) {
        this.acceptorHost = acceptorHost;
    }

    @Override
    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public String getParentApplicationName() {
        return parentApplicationName;
    }

    public void setParentApplicationName(String parentApplicationName) {
        this.parentApplicationName = parentApplicationName;
    }

    @Override
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String getParentApplicationType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getSamplingFlag() {
        return samplingFlag;
    }

    public void setSamplingFlag(String samplingFlag) {
        this.samplingFlag = samplingFlag;
    }

    @Override
    public String getParentApplicationNamespace() {
        return parentApplicationNamespace;
    }

    public void setParentApplicationNamespace(String parentApplicationNamespace) {
        this.parentApplicationNamespace = parentApplicationNamespace;
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public long getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(long parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    @Override
    public long getSpanId() {
        return spanId;
    }

    public void setSpanId(long spanId) {
        this.spanId = spanId;
    }

    @Override
    public short getFlags() {
        return flags;
    }

    public void setFlags(short flags) {
        this.flags = flags;
    }

    @Override
    public Map<String, String> getProxyHeaderMap() {
        return proxyHeaderMap;
    }

    public void setProxyHeaderMap(Map<String, String> proxyHeaderMap) {
        this.proxyHeaderMap = proxyHeaderMap;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultServerRequestWrapper{");
        sb.append("rpcName='").append(rpcName).append('\'');
        sb.append(", endPoint='").append(endPoint).append('\'');
        sb.append(", remoteAddress='").append(remoteAddress).append('\'');
        sb.append(", acceptorHost='").append(acceptorHost).append('\'');
        sb.append(", method='").append(method).append('\'');
        sb.append(", parameters='").append(parameters).append('\'');
        sb.append(", parentApplicationName='").append(parentApplicationName).append('\'');
        sb.append(", host='").append(host).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", samplingFlag='").append(samplingFlag).append('\'');
        sb.append(", parentApplicationNamespace='").append(parentApplicationNamespace).append('\'');
        sb.append(", transactionId='").append(transactionId).append('\'');
        sb.append(", parentSpanId=").append(parentSpanId);
        sb.append(", spanId=").append(spanId);
        sb.append(", flags=").append(flags);
        sb.append(", proxyHeaderMap=").append(proxyHeaderMap);
        sb.append('}');
        return sb.toString();
    }
}