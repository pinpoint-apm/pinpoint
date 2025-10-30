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

package com.navercorp.pinpoint.service.dao.mysql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;


class MysqlServiceDaoTest {

    @Test
    void mapper() throws JsonProcessingException {
        ObjectMapper objectMapper = Jackson.newMapper();

        Map<String, String> map = Map.of("key", "value");
        String json = objectMapper.writeValueAsString(map);

        MysqlServiceDao.Mapper mapper = new MysqlServiceDao.Mapper(objectMapper);
        Map<String, String> readerMap =  mapper.fromJson(json);

        Assertions.assertEquals(map, readerMap);
    }

}