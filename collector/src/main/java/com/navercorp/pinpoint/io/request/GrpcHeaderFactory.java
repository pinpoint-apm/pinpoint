package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.io.DefaultServerHeader;
import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.grpc.Header;

import java.util.function.Supplier;

public class GrpcHeaderFactory {
    public GrpcHeaderFactory() {
    }

    public ServerHeader serverHeader(Header header, Supplier<ServiceUid> uidFetcher) {
        return new DefaultServerHeader(
                header.getAgentId(),
                header.getAgentName(),
                header.getApplicationName(),
                header.getServiceName(),
                uidFetcher,
                header.getAgentStartTime(),
                header.getServiceType(),
                header.isGrpcBuiltInRetry()
        );
    }
}
