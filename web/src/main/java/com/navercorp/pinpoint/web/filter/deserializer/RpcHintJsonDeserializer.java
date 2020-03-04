/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.filter.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.navercorp.pinpoint.web.filter.RpcHint;
import com.navercorp.pinpoint.web.filter.RpcType;
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
            ctxt.handleUnexpectedToken(RpcHint.class, jp);
        }
        // skip start array
        final JsonToken token = jp.nextToken();
        // [] empty array
        if (token == JsonToken.END_ARRAY) {
            return new RpcHint(applicationName, Collections.emptyList());
        }
        final List<RpcType> rpcHintList = new ArrayList<>();
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
