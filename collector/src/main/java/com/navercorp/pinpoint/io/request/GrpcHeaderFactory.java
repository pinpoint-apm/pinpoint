package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.io.DefaultServerHeader;
import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.uid.ServiceUidSupplier;
import com.navercorp.pinpoint.grpc.Header;


public class GrpcHeaderFactory {
    public GrpcHeaderFactory() {
    }

    public ServerHeader serverHeader(Header header, ServiceUidSupplier uidSupplier) {
        return new DefaultServerHeader(
                header.getAgentId(),
                header.getAgentName(),
                header.getApplicationName(),
                header.getServiceName(),
                uidSupplier,
                header.getAgentStartTime(),
                header.getServiceType(),
                header.isGrpcBuiltInRetry()
        );
    }
}
