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
import com.navercorp.pinpoint.web.filter.RpcHint;
import com.navercorp.pinpoint.web.filter.RpcType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 */
public class RpcHintJsonDeserializer extends ValueDeserializer<RpcHint> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public RpcHint deserialize(JsonParser jp, DeserializationContext ctxt) {
        final String applicationName = jp.getString();

        if (jp.nextToken() != JsonToken.START_ARRAY) {
            ctxt.handleUnexpectedToken(RpcHint.class, jp);
        }
        // skip start array
        // [] empty array
        if (jp.nextToken() == JsonToken.END_ARRAY) {
            return new RpcHint(applicationName, Collections.emptyList());
        }
        final List<RpcType> rpcHintList = new ArrayList<>();
        do {
            RpcType rpcType = jp.readValueAs(RpcType.class);
            rpcHintList.add(rpcType);
        } while (jp.nextToken() != JsonToken.END_ARRAY);
        return new RpcHint(applicationName, rpcHintList);
    }

}
