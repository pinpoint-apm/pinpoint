package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.io.IOException;

public class ServiceTypeDescView extends JsonSerializer<ServiceType> {

    @Override
    public void serialize(ServiceType serviceType, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeObject(serviceType.getDesc());
    }
}
