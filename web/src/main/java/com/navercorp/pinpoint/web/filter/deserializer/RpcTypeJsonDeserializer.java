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
