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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.navercorp.pinpoint.web.filter.deserializer.RpcTypeJsonDeserializer;

/**
 * @author emeroad
 */
@JsonDeserialize(using = RpcTypeJsonDeserializer.class)
public class RpcType {
    private final String address;
    private final int spanEventServiceTypeCode;

    public RpcType(String address, int spanEventServiceTypeCode) {
        if (address == null) {
            throw new NullPointerException("address must not be null");
        }
        this.address = address;
        this.spanEventServiceTypeCode = spanEventServiceTypeCode;
    }

    public String getAddress() {
        return address;
    }

    public int getSpanEventServiceTypeCode() {
        return spanEventServiceTypeCode;
    }

    public boolean isMatched(String address, int serviceTypeCode) {
        return this.address.equals(address) && this.spanEventServiceTypeCode == serviceTypeCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RpcType{");
        sb.append("address='").append(address).append('\'');
        sb.append(", spanEventServiceTypeCode=").append(spanEventServiceTypeCode);
        sb.append('}');
        return sb.toString();
    }
}
