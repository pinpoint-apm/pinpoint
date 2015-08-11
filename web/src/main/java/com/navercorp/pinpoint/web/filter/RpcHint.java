package com.navercorp.pinpoint.web.filter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.navercorp.pinpoint.web.filter.deserializer.RpcHintJsonDeserializer;

import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 */
@JsonDeserialize(using = RpcHintJsonDeserializer.class)
public class RpcHint {

    private final String applicationName;
    // TODO fix serviceType miss
//        private ServiceType serviceType;
    private final List<RpcType> rpcTypeList;

    public RpcHint(String applicationName, List<RpcType> rpcTypeList) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (rpcTypeList == null) {
            throw new NullPointerException("rpcTypeList must not be null");
        }
        this.applicationName = applicationName;
        this.rpcTypeList = Collections.unmodifiableList(rpcTypeList);
    }


    public String getApplicationName() {
        return applicationName;
    }

    public List<RpcType> getRpcTypeList() {
        return rpcTypeList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RpcHint{");
        sb.append("applicationName='").append(applicationName).append('\'');
        sb.append(", rpcTypeList=").append(rpcTypeList);
        sb.append('}');
        return sb.toString();
    }
}
