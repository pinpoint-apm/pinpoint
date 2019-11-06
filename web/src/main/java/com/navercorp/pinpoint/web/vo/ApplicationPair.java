/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author HyunGil Jeong
 */
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"applicationName", "serviceTypeCode"})
public class ApplicationPair {

    private String applicationName;
    private short serviceTypeCode;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public short getServiceTypeCode() {
        return serviceTypeCode;
    }

    public void setServiceTypeCode(short serviceTypeCode) {
        this.serviceTypeCode = serviceTypeCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ApplicationPair{");
        sb.append("applicationName='").append(applicationName).append('\'');
        sb.append(", serviceTypeCode=").append(serviceTypeCode);
        sb.append('}');
        return sb.toString();
    }
}
