/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class MapStructUtilsTest {

    @Test
    void jsonToLongList() {
        ObjectMapper mapper = Jackson.newMapper();
        MapStructUtils mapStructUtils = new MapStructUtils(mapper);

        List<Long> longs = mapStructUtils.jsonToLongList("[1,2,3]");
        assertThat(longs).containsExactly(1L, 2L, 3L);
    }

    @Test
    void jsonStrToIntegerList() {
        ObjectMapper mapper = Jackson.newMapper();
        MapStructUtils mapStructUtils = new MapStructUtils(mapper);

        List<Integer> integers = mapStructUtils.jsonToIntegerList("[1,2,3]");
        assertThat(integers).containsExactly(1, 2, 3);
    }

    @Test
    void jsonStrToStringList() {
        ObjectMapper mapper = Jackson.newMapper();
        MapStructUtils mapStructUtils = new MapStructUtils(mapper);

        List<String> strings = mapStructUtils.jsonToStringList("[1,2,3]");
        assertThat(strings).containsExactly("1", "2", "3");
    }

    @Test
    void jsonToStringMapList() {
        ObjectMapper mapper = Jackson.newMapper();
        MapStructUtils mapStructUtils = new MapStructUtils(mapper);

        List<Map<String, String>> maps = mapStructUtils.jsonToStringMapList("[{\"key\": \"value\"}, {\"key2\": \"value2\"}, {\"key3\": \"value3\"}]");
        assertThat(maps).containsExactlyInAnyOrder(
                Map.of("key", "value"),
                Map.of("key2", "value2"),
                Map.of("key3", "value3")
        );
    }


    @Test
    void stringListToJson() throws JsonProcessingException {
        ObjectMapper mapper = Jackson.newMapper();
        MapStructUtils mapStructUtils = new MapStructUtils(mapper);

        List<String> list = List.of("1", "2", "3");
        String json = mapStructUtils.listToJsonStr(list);
        assertThat(json).isEqualTo(mapper.writeValueAsString(list));
    }

    @Test
    void integerListToJson() throws JsonProcessingException {
        ObjectMapper mapper = Jackson.newMapper();
        MapStructUtils mapStructUtils = new MapStructUtils(mapper);


        List<Integer> list = List.of(1, 2, 3);
        String json = mapStructUtils.listToJsonStr(list);
        assertThat(json).isEqualTo(mapper.writeValueAsString(list));
    }
}