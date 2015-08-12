package com.navercorp.pinpoint.web.filter.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.navercorp.pinpoint.web.filter.RpcType;

import java.io.IOException;

/**
 * @author emeroad
 */
public class RpcTypeJsonDeserializer extends JsonDeserializer<RpcType> {


    public RpcTypeJsonDeserializer() {
    }

    @Override
    public RpcType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String address = jp.getText();
        jp.nextToken();
        int serviceCode = jp.getIntValue();
        return new RpcType(address, serviceCode);
    }
}
