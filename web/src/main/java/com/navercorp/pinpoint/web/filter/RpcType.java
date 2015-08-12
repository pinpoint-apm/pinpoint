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
