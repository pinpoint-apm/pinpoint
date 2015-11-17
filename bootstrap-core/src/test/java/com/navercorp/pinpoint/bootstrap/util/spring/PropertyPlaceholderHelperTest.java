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

package com.navercorp.pinpoint.bootstrap.util.spring;

import org.junit.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.util.spring.PropertyPlaceholderHelper;

import java.util.Properties;

public class PropertyPlaceholderHelperTest {

    @Test
    public void testReplacePlaceholders() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("test", "a");

        PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}");
        String value1 = helper.replacePlaceholders("${test}", properties);
        Assert.assertEquals("a", value1);

        String value2 = helper.replacePlaceholders("123${test}456", properties);
        Assert.assertEquals("123a456", value2);

        String value3 = helper.replacePlaceholders("123${test}456${test}", properties);
        Assert.assertEquals("123a456a", value3);
    }
}