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

package com.navercorp.pinpoint.test.plugin.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Woonduk Kang(emeroad)
 * @author WonChul Heo(heowc)
 */
public class StringUtilsTest {

    @Test
    public void testIsEmpty() {
        assertThat(StringUtils.isEmpty(null), is(true));
        assertThat(StringUtils.isEmpty(""), is(true));
        assertThat(StringUtils.isEmpty(" "), is(false));
        assertThat(StringUtils.isEmpty("pinpoint"), is(false));
    }

    @Test
    public void testHasLength() {
        assertThat(StringUtils.hasLength(null), is(false));
        assertThat(StringUtils.hasLength(""), is(false));
        assertThat(StringUtils.hasLength(" "), is(true));
        assertThat(StringUtils.hasLength("pinpoint"), is(true));
    }

    @Test
    public void testHasText() {
        assertThat(StringUtils.hasText(null), is(false));
        assertThat(StringUtils.hasText(""), is(false));
        assertThat(StringUtils.hasText(" "), is(false));
        assertThat(StringUtils.hasText(" pinpoint"), is(true));
        assertThat(StringUtils.hasText("pinpoint"), is(true));
        assertThat(StringUtils.hasText("pinpoint "), is(true));
    }

    @Test
    public void testJoinList() {
        String join = StringUtils.join(Arrays.asList("1", "2"), ",");
        assertThat(join, is("1,2"));
    }

    @Test
    public void testJoinMap() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("c", "d");
        map.put("a", "b");
        String join = StringUtils.join(map, "=", "&");
        assertThat(join, is("c=d&a=b"));
    }
}