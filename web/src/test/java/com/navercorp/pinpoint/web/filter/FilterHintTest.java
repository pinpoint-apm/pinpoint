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

package com.navercorp.pinpoint.web.filter;

import org.junit.Assert;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.web.filter.FilterHint;

/**
 * 
 * @author netspider
 * 
 */
public class FilterHintTest {
    private final ObjectMapper om = new ObjectMapper();

    @Test
    public void convert() {
        StringBuilder json = new StringBuilder();
        json.append("{ \"TO_APPLICATION\" : [\"IP1\", 1,\"IP2\", 2], \"TO_APPLICATION2\" : [\"IP3\", 3,\"IP4\", 4] }");

        try {
            FilterHint hint = om.readValue(json.toString(), new TypeReference<FilterHint>() {
            });

            Assert.assertNotNull(hint);
            Assert.assertEquals(2, hint.size());

            Assert.assertTrue(hint.containApplicationHint("TO_APPLICATION"));
            Assert.assertTrue(hint.containApplicationHint("TO_APPLICATION2"));
            Assert.assertFalse(hint.containApplicationHint("TO_APPLICATION3"));

            Assert.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION", "IP1", 1));
            Assert.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION", "IP2", 2));

            Assert.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION2", "IP3", 3));
            Assert.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION2", "IP4", 4));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void empty() {
        StringBuilder json = new StringBuilder();
        json.append("{}");

        try {
            FilterHint hint = om.readValue(json.toString(), new TypeReference<FilterHint>() {
            });

            Assert.assertNotNull(hint);
            Assert.assertTrue(hint.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
