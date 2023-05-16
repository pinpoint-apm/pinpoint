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

package com.navercorp.pinpoint.profiler.context;


/**
 * 存放请求/响应报文、url、头信息的span扩展块
 *
 * @author wangj881
 */
public class WebInfo extends DefaultFrameAttachment {

    private String requestUrl;

    private Object requestBody;

    private Object requestHeader;

    private Object responseBody;

    private Object responseHeader;

    private String requestMethod;

    private int statusCode;

    private String parentApplicationName;

    /**
     * 判断是否有报文采集的标志位
     */
    private boolean flag = false;

    /**
     * 报文状态标志：
     * 0代表正常，1代表异常，2代表未知
     */
    private byte status = 2;

    /**
     * 采样策略
     */
    private byte webBodyStrategy = 2;

    /**
     * 判断该次采集是disableTrace还是defaultTrace，默认是defaultTrace
     */
    private boolean disabled = false;

    /**
     * 耗时
     */
    private int elapsedTime;

    @Override
    public String toString() {
        return "WebInfo{" +
                "requestUrl='" + requestUrl + '\'' +
                ", requestBody=" + requestBody +
                ", requestHeader=" + requestHeader +
                ", responseBody=" + responseBody +
                ", responseHeader=" + responseHeader +
                ", requestMethod='" + requestMethod + '\'' +
                ", statusCode=" + statusCode +
                ", parentApplicationName='" + parentApplicationName + '\'' +
                ", flag=" + flag +
                ", status=" + status +
                ", webBodyStrategy=" + webBodyStrategy +
                ", disabled=" + disabled +
                ", elapsedTime=" + elapsedTime +
                '}';
    }

    public String getParentApplicationName() {
        return parentApplicationName;
    }

    public void setParentApplicationName(String parentApplicationName) {
        this.parentApplicationName = parentApplicationName;
    }

    public WebInfo() {
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public Object getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
        setFlag(true);
    }

    public Object getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(Object requestHeader) {
        this.requestHeader = requestHeader;
        setFlag(true);
    }

    public Object getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(Object responseBody) {
        this.responseBody = responseBody;
        setFlag(true);
    }

    public Object getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(Object responseHeader) {
        this.responseHeader = responseHeader;
        setFlag(true);
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

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public byte getWebBodyStrategy() {
        return webBodyStrategy;
    }

    public void setWebBodyStrategy(byte webBodyStrategy) {
        this.webBodyStrategy = webBodyStrategy;
    }
}
