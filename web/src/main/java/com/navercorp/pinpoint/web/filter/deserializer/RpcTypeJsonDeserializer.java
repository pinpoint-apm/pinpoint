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

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import com.navercorp.pinpoint.web.filter.RpcType;

/**
 * @author emeroad
 */
public class RpcTypeJsonDeserializer extends ValueDeserializer<RpcType> {


    public RpcTypeJsonDeserializer() {
    }

    @Override
    public RpcType deserialize(JsonParser jp, DeserializationContext ctxt) {
        if (jp.currentToken() != JsonToken.VALUE_STRING) {
            ctxt.handleUnexpectedToken(RpcType.class, jp);
        }
        String address = jp.getText();
        if (jp.nextToken() != JsonToken.VALUE_NUMBER_INT) {
            ctxt.handleUnexpectedToken(RpcType.class, jp);
        }
        int serviceCode = jp.getIntValue();
        return new RpcType(address, serviceCode);
    }
}
