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

package com.navercorp.pinpoint.web.filter;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.navercorp.pinpoint.common.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author netspider
 *
 */
public class FilterDescriptor {

    /**
     * from application
     */
    private String fromApplicationName = null;
    private String fromServiceType = null;
    private String fromAgentId = null;
    private Long fromResponseTime = null;

    /**
     * requested url
     */
    private String urlPattern = null;

    /**
     * to application
     */
    private String toApplicationName = null;
    private String toServiceType = null;
    private String toAgentId = null;
    private String toResponseTime = null;

    /**
     * include exception
     */
    private Boolean includeException = null;

    public FilterDescriptor() {
    }

    public boolean isValid() {
        return isValidFromToInfo() && isValidFromToResponseTime();
    }

    public boolean isValidFromToInfo() {
        return !(StringUtils.isEmpty(fromApplicationName) || StringUtils.isEmpty(fromServiceType) || StringUtils.isEmpty(toApplicationName) || StringUtils.isEmpty(toServiceType));
    }

    public boolean isValidFromToResponseTime() {
        return !((fromResponseTime == null && !StringUtils.isEmpty(toResponseTime)) || (fromResponseTime != null && StringUtils.isEmpty(toResponseTime)));
    }

    public boolean isSetUrl() {
        return !StringUtils.isEmpty(urlPattern);
    }


    public Long getResponseTo() {
        if (toResponseTime == null) {
            return null;
        } else if ("max".equals(toResponseTime)) {
            return Long.MAX_VALUE;
        } else {
            return Long.valueOf(toResponseTime);
        }
    }


    public String getUrlPattern() {
        return urlPattern;
    }

    public String getFromApplicationName() {
        return fromApplicationName;
    }

    @JsonSetter(value = "fa")
    public void setFromApplicationName(String fromApplicationName) {
        this.fromApplicationName = fromApplicationName;
    }

    public String getFromServiceType() {
        return fromServiceType;
    }

    @JsonSetter(value = "fst")
    public void setFromServiceType(String fromServiceType) {
        this.fromServiceType = fromServiceType;
    }

    public String getToApplicationName() {
        return toApplicationName;
    }

    @JsonSetter(value = "ta")
    public void setToApplicationName(String toApplicationName) {
        this.toApplicationName = toApplicationName;
    }

    public String getToServiceType() {
        return toServiceType;
    }

    @JsonSetter(value = "tst")
    public void setToServiceType(String toServiceType) {
        this.toServiceType = toServiceType;
    }

    public Long getFromResponseTime() {
        return fromResponseTime;
    }

    @JsonSetter(value = "rf")
    public void setFromResponseTime(Long fromResponseTime) {
        this.fromResponseTime = fromResponseTime;
    }

    public String getToResponseTime() {
        return toResponseTime;
    }

    @JsonSetter(value = "rt")
    public void setToResponseTime(String toResponseTime) {
        this.toResponseTime = toResponseTime;
    }

    public Boolean getIncludeException() {
        return includeException;
    }

    @JsonSetter(value = "ie")
    public void setIncludeException(Boolean includeException) {
        this.includeException = includeException;
    }

    @JsonSetter(value = "url")
    public void setUrlPattern(String urlPattern) {
        this.urlPattern = decodeBase64(urlPattern);
    }

    private String decodeBase64(String urlPattern) {
        if (urlPattern == null) {
            return null;
        }
        return new String(Base64.decodeBase64(urlPattern), Charsets.UTF_8);
    }

    public String getFromAgentName() {
        return fromAgentId;
    }

    @JsonSetter(value = "fan")
    public void setFromAgentId(String fromAgentId) {
        this.fromAgentId = fromAgentId;
    }

    public String getToAgentName() {
        return toAgentId;
    }

    @JsonSetter(value = "tan")
    public void setToAgentId(String toAgentId) {
        this.toAgentId = toAgentId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FilterDescriptor{");
        sb.append("fromApplicationName='").append(fromApplicationName).append('\'');
        sb.append(", fromServiceType='").append(fromServiceType).append('\'');
        sb.append(", toApplicationName='").append(toApplicationName).append('\'');
        sb.append(", toServiceType='").append(toServiceType).append('\'');
        sb.append(", fromResponseTime=").append(fromResponseTime);
        sb.append(", toResponseTime='").append(toResponseTime).append('\'');
        sb.append(", includeException=").append(includeException);
        sb.append(", urlPattern='").append(urlPattern).append('\'');
        sb.append(", fromAgentId='").append(fromAgentId).append('\'');
        sb.append(", toAgentId='").append(toAgentId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
