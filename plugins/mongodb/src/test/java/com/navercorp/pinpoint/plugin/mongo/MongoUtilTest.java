/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.navercorp.pinpoint.common.util.StringStringValue;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * @author Woonduk Kang(emeroad)
 */
public class MongoUtilTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void parseBson() throws IOException {
        BasicDBObject query = new BasicDBObject();
        query.put("query1", new BasicDBObject("$gt", 1));

        StringStringValue stringStringValue = MongoUtil.parseBson(new Object[]{query}, true);

        List list = objectMapper.readValue("[" + stringStringValue.getStringValue1()+"]", List.class);
        Assert.assertEquals(list.size(), 1);
        Map<String, ?> query1Map = (Map<String, ?>) list.get(0);
        Assert.assertEquals(query1Map.get("query1"), "?");
    }

    @Test
    public void parsedBson2() throws IOException {
        BasicDBObject query = new BasicDBObject();
        query.put("query1", new BasicDBObject("$gt", 1));

        BasicDBObject query2 = new BasicDBObject();
        query2.put("query2", new BasicDBObject("$gt", 2));

        StringStringValue stringStringValue = MongoUtil.parseBson(new Object[]{query, query2}, true);
        logger.debug("{}", stringStringValue);

        List list = objectMapper.readValue("[" + stringStringValue.getStringValue1()+"]", List.class);
        logger.debug("list:{}", stringStringValue.getStringValue1());
        logger.debug("list:{}", list);

        Assert.assertEquals(list.size(), 2);
        Map<String, ?> query1Map = (Map<String, ?>) list.get(0);
        Assert.assertEquals(query1Map.get("query1"), "?");

        Map<String, ?> query2Map = (Map<String, ?>) list.get(1);
        Assert.assertEquals(query2Map.get("query2"), "?");
    }
}