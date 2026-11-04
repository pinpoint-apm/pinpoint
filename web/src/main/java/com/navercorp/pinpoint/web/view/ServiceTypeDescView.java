package com.navercorp.pinpoint.web.view;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import com.navercorp.pinpoint.common.trace.ServiceType;

public class ServiceTypeDescView extends ValueSerializer<ServiceType> {

    @Override
    public void serialize(ServiceType serviceType, JsonGenerator jgen, SerializationContext provider) {
        jgen.writePOJO(serviceType.getDesc());
    }
}
