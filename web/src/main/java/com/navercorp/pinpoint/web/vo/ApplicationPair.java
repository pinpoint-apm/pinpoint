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

/**
 * @author HyunGil Jeong
 */
public class ApplicationPair {

    private String fromApplicationName;
    private short fromServiceTypeCode;
    private String toApplicationName;
    private short toServiceTypeCode;

    public String getFromApplicationName() {
        return fromApplicationName;
    }

    public void setFromApplicationName(String fromApplicationName) {
        this.fromApplicationName = fromApplicationName;
    }

    public short getFromServiceTypeCode() {
        return fromServiceTypeCode;
    }

    public void setFromServiceTypeCode(short fromServiceTypeCode) {
        this.fromServiceTypeCode = fromServiceTypeCode;
    }

    public String getToApplicationName() {
        return toApplicationName;
    }

    public void setToApplicationName(String toApplicationName) {
        this.toApplicationName = toApplicationName;
    }

    public short getToServiceTypeCode() {
        return toServiceTypeCode;
    }

    public void setToServiceTypeCode(short toServiceTypeCode) {
        this.toServiceTypeCode = toServiceTypeCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NodeKeysParam{");
        sb.append("fromApplicationName='").append(fromApplicationName).append('\'');
        sb.append(", fromServiceTypeCode=").append(fromServiceTypeCode);
        sb.append(", toApplicationName='").append(toApplicationName).append('\'');
        sb.append(", toServiceTypeCode=").append(toServiceTypeCode);
        sb.append('}');
        return sb.toString();
    }
}
