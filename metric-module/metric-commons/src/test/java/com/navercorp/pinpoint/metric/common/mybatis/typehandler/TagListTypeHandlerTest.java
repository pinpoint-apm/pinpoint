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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.Tags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


public class TagListTypeHandlerTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void test() throws JsonProcessingException {
        ObjectMapper mapper = TagListTypeHandler.getMapper();

        List<Tag> list = List.of(
                new Tag("a", "1"),
                new Tag("a", "2")
        );

        String json = mapper.writeValueAsString(list);
        logger.debug("serialize:{}", json);

        Tags tags = mapper.readValue(json, Tags.class);
        logger.debug("deserialize:{}", tags.getTags());

        Assertions.assertEquals(list, tags.getTags());
    }
}