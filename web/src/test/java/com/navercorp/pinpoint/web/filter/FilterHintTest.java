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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 *
 * @author netspider
 *
 */
public class FilterHintTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void convert() throws IOException {

        String json = "{ \"TO_APPLICATION\" : [\"IP1\", 1,\"IP2\", 2], \"TO_APPLICATION2\" : [\"IP3\", 3,\"IP4\", 4] }";

        final FilterHint hint = mapper.readValue(json, FilterHint.class);

        Assertions.assertNotNull(hint);
        Assertions.assertEquals(2, hint.size());

        Assertions.assertTrue(hint.containApplicationHint("TO_APPLICATION"));
        Assertions.assertTrue(hint.containApplicationHint("TO_APPLICATION2"));
        Assertions.assertFalse(hint.containApplicationHint("TO_APPLICATION3"));

        Assertions.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION", "IP1", 1));
        Assertions.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION", "IP2", 2));

        Assertions.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION2", "IP3", 3));
        Assertions.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION2", "IP4", 4));
    }

    @Test
    public void convert_duplicate_applicationName_filter() throws IOException {

        String json = "{ \"TO_APPLICATION\" : [\"IP1\", 1,\"IP2\", 2], \"TO_APPLICATION\" : [\"IP3\", 3,\"IP4\", 4] }";


        final FilterHint hint = mapper.readValue(json, FilterHint.class);

        Assertions.assertNotNull(hint);
        Assertions.assertEquals(2, hint.size());

        Assertions.assertTrue(hint.containApplicationHint("TO_APPLICATION"));
        Assertions.assertFalse(hint.containApplicationHint("TO_APPLICATION2"));

        Assertions.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION", "IP1", 1));
        Assertions.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION", "IP2", 2));

        Assertions.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION", "IP3", 3));
        Assertions.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION", "IP4", 4));
    }

    @Test
    public void empty() throws IOException {
        String json = "{}";

        final FilterHint hint = mapper.readValue(json, FilterHint.class);

        Assertions.assertNotNull(hint);
        Assertions.assertEquals(0, hint.size());

    }

    @Test
    public void empty_array() throws IOException {

        String json = "{ \"TO_APPLICATION\" : [] }";


        final FilterHint hint = mapper.readValue(json, FilterHint.class);

        Assertions.assertNotNull(hint);
        Assertions.assertEquals(1, hint.size());
        Assertions.assertTrue(hint.getRpcHintList("TO_APPLICATION").get(0).getRpcTypeList().isEmpty());

        Assertions.assertTrue(hint.containApplicationHint("TO_APPLICATION"));
        Assertions.assertFalse(hint.containApplicationHint("TO_APPLICATION2"));

        Assertions.assertFalse(hint.containApplicationEndpoint("TO_APPLICATION", "IP1", 1));

    }

    @Test
    public void empty_array2() throws IOException {

        String json = "{ \"TO_APPLICATION\" : [], \"TO_APPLICATION2\" : [\"IP3\", 3,\"IP4\", 4] }";

        final FilterHint hint = mapper.readValue(json, FilterHint.class);

        Assertions.assertNotNull(hint);
        Assertions.assertEquals(2, hint.size());

        Assertions.assertTrue(hint.containApplicationHint("TO_APPLICATION"));
        Assertions.assertTrue(hint.containApplicationHint("TO_APPLICATION2"));

        Assertions.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION2", "IP3", 3));
        Assertions.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION2", "IP4", 4));

    }
}
