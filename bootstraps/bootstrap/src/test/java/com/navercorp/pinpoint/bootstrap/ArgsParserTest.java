/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ArgsParserTest {

    @Test
    public void parse() {
        ArgsParser parser = new ArgsParser();
        Map<String, String> parameter = parser.parse("a=1,b=2");
        Assert.assertEquals("1", parameter.get("a"));
        Assert.assertEquals("2", parameter.get("b"));
    }

    @Ignore
    @Test
    public void parse_comma() {
        // TODO
        ArgsParser parser = new ArgsParser();
        Map<String, String> parameter = parser.parse("a=1\\,234,b=2");
        Assert.assertEquals("1,234", parameter.get("a"));
        Assert.assertEquals("2", parameter.get("b"));
    }

}