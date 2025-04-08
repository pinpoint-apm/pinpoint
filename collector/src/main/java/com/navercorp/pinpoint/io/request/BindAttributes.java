package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.bo.grpc.BindAttribute;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;

public class BindAttributes {

    public static BindAttribute of(ServerHeader header, long acceptedTime) {
        return new BindAttribute(header.getAgentId(),
                header.getAgentName(),
                header.getApplicationName(),
                // TODO Apply ApplicationUidSupplier
                () -> ApplicationUid.ERROR_APPLICATION_UID,
                header.getAgentStartTime(),
                acceptedTime);
    }

}
