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
import com.navercorp.pinpoint.web.filter.FilterHint;
import com.navercorp.pinpoint.web.filter.RpcHint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 */
public class FilterHintListJsonDeserializer extends JsonDeserializer<FilterHint> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public FilterHint deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (!jp.getCurrentToken().isStructStart()) {
            throw ctxt.mappingException(RpcHint.class, jp.getCurrentToken());
        }
        // skip json start
        final JsonToken jsonToken = jp.nextToken();
        if (jsonToken == JsonToken.END_OBJECT) {
            return new FilterHint(Collections.<RpcHint>emptyList());
        }


        List<RpcHint> rpcHintList = new ArrayList<>();
        while (true) {
            final RpcHint rpcHint = jp.readValueAs(RpcHint.class);
            rpcHintList.add(rpcHint);
            if (jp.nextToken() == JsonToken.END_OBJECT) {
                break;
            }
        }
        return new FilterHint(rpcHintList);
    }
}
