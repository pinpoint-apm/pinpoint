package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.bo.grpc.BindAttribute;

public class BindAttributes {

    public static BindAttribute of(ServerHeader header, long acceptedTime) {
        return new BindAttribute(header.getAgentId(),
                header.getApplicationName(),
                header.getAgentStartTime(),
                acceptedTime);
    }

}
