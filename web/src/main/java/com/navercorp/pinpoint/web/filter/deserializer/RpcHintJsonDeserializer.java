package com.navercorp.pinpoint.web.filter.deserializer;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.navercorp.pinpoint.web.filter.RpcHint;
import com.navercorp.pinpoint.web.filter.RpcType;
import org.apache.hadoop.ipc.RPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 */
public class RpcHintJsonDeserializer extends JsonDeserializer<RpcHint> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public RpcHint deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final String applicationName = jp.getText();

        if (jp.nextToken() != JsonToken.START_ARRAY) {
            throw ctxt.mappingException(RpcHint.class, jp.getCurrentToken());
        }
        // skip start array
        final JsonToken token = jp.nextToken();
        // [] empty array
        if (token == JsonToken.END_ARRAY) {
            return new RpcHint(applicationName, Collections.<RpcType>emptyList());
        }
        final List<RpcType> rpcHintList = new ArrayList<RpcType>();
        while (true) {
            RpcType rpcType =  jp.readValueAs(RpcType.class);
            rpcHintList.add(rpcType);
            if (jp.nextToken() == JsonToken.END_ARRAY) {
                break;
            }
        }
        return new RpcHint(applicationName, rpcHintList);
    }

}
