/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.metric.common.mybatis.typehandler;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.metric.common.model.Tag;

import java.io.IOException;
import java.util.List;

public class TagMySqlSerializer extends JsonSerializer<List<Tag>> {

    @Override
    public void serialize(List<Tag> tags, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        for (Tag tag : tags) {
            gen.writeStringField(tag.getName(), tag.getValue());
        }
        gen.writeEndObject();
    }

}
