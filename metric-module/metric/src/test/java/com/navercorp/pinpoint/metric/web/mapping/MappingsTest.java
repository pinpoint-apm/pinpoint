/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.MatchingRule;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;


public class MappingsTest {

    @Test
    public void test2() throws IOException {
        InputStream resource = getClass().getResourceAsStream("/pinot-web/telegraf-metric.yml");

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        Mappings mappings = mapper.readValue(resource, Mappings.class);
        Metric metric = mappings.getMappings().get(0);
        Assert.assertEquals("cpu", metric.getName());
        Assert.assertEquals("usage_user", metric.getFields().get(0).getName());
//        Assert.assertEquals("");
    }

}