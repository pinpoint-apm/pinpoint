/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.shared;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;


/**
 * @author Woonduk Kang(emeroad)
 */
public class TestParameterParserTest {

    @Test
    public void parse() {
        TestParameterParser parser = new TestParameterParser();
        List<TestParameter> parameters = parser.parse(new String[] {"testId=dependency1"});
        Assert.assertEquals(parameters.size(), 1);
        TestParameter one = parameters.get(0);
        Assert.assertEquals(one.getTestId(), "testId" );
        Assert.assertEquals(one.getMavenDependencies(), "dependency1" );
    }
}