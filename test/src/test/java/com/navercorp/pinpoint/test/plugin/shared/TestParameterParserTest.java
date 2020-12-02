/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.shared;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Woonduk Kang(emeroad)
 * @author WonChul Heo(heowc)
 */
public class TestParameterParserTest {

    @Test
    public void testParse() {
        final TestParameterParser parser = new TestParameterParser();
        final List<TestParameter> parameters = parser.parse(new String[]{"testId=dependency1"});
        assertThat(parameters.size(), is(1));
        final TestParameter one = parameters.get(0);
        assertThat(one.getTestId(), is("testId"));
        assertThat(one.getMavenDependencies(), is("dependency1"));
    }

    @Test
    public void testParseWhenNull() {
        final TestParameterParser parser = new TestParameterParser();
        final List<TestParameter> parameters = parser.parse(null);
        assertThat(parameters.size(), is(0));
    }

    @Test
    public void testParseWhenEmpty() {
        final TestParameterParser parser = new TestParameterParser();
        final List<TestParameter> parameters = parser.parse(new String[]{});
        assertThat(parameters.size(), is(0));
    }
}