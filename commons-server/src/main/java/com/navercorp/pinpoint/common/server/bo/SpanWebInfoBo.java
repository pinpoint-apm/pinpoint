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

import com.navercorp.pinpoint.common.profiler.util.TransactionId;


/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanWebInfoBo implements BasicSpan {

    private byte version = 0;

    private String agentId;
    private String applicationId;
    private long agentStartTime;

    private TransactionId transactionId;

    private long spanId;
    private long parentSpanId;
    private String requestBody;
    private String requestUrl;
    private String requestHeader;
    private String responseBody;
    private String responseHeader;
    private long collectorAcceptTime;

    private String parentApplicationName;

    /**
     * 报文异常标志
     */
    private byte busiCode;
    /**
     * 请求方法
     */
    private String requestMethod;
    /**
     * 响应码
     */
    private int statusCode;
    /**
     * 采样策略
     */
    private byte httpMsgStrategy;

    /**
     * 耗时
     */
    private int elapsedTime;



    public SpanWebInfoBo() {
    }

    @Override
    public int getVersion() {
        return version & 0xFF;
    }

    public void setVersion(int version) {
        SpanBo.checkVersion(version);
        // check range
        this.version = (byte) (version & 0xFF);
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public String getApplicationId() {
        return applicationId;
    }

    @Override
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public long getAgentStartTime() {
        return agentStartTime;
    }

    @Override
    public void setAgentStartTime(long agentStartTime) {
        this.agentStartTime = agentStartTime;
    }

    @Override
    public TransactionId getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(TransactionId transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public long getSpanId() {
        return spanId;
    }

    @Override
    public void setSpanId(long spanId) {
        this.spanId = spanId;
    }


    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(String requestHeader) {
        this.requestHeader = requestHeader;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(String responseHeader) {
        this.responseHeader = responseHeader;
    }

    public long getCollectorAcceptTime() {
        return collectorAcceptTime;
    }

    public void setCollectorAcceptTime(long collectorAcceptTime) {
        this.collectorAcceptTime = collectorAcceptTime;
    }

    public long getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(long parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public byte getHttpMsgStrategy() {
        return httpMsgStrategy;
    }

    public void setHttpMsgStrategy(byte httpMsgStrategy) {
        this.httpMsgStrategy = httpMsgStrategy;
    }

    public byte getBusiCode() {
        return busiCode;
    }

    public void setBusiCode(byte busiCode) {
        this.busiCode = busiCode;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getParentApplicationName() {
        return parentApplicationName;
    }

    public void setParentApplicationName(String parentApplicationName) {
        this.parentApplicationName = parentApplicationName;
    }

    @Override
    public String toString() {
        return "SpanWebInfoBo{" +
                "version=" + version +
                ", agentId='" + agentId + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", agentStartTime=" + agentStartTime +
                ", transactionId=" + transactionId +
                ", spanId=" + spanId +
                ", parentSpanId=" + parentSpanId +
                ", requestBody='" + requestBody + '\'' +
                ", requestUrl='" + requestUrl + '\'' +
                ", requestHeader='" + requestHeader + '\'' +
                ", responseBody='" + responseBody + '\'' +
                ", responseHeader='" + responseHeader + '\'' +
                ", collectorAcceptTime=" + collectorAcceptTime +
                ", parentApplicationName='" + parentApplicationName + '\'' +
                ", busiCode=" + busiCode +
                ", requestMethod='" + requestMethod + '\'' +
                ", statusCode=" + statusCode +
                ", httpMsgStrategy=" + httpMsgStrategy +
                ", elapsedTime=" + elapsedTime +
                '}';
    }
}
