/*
 * Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.rpc.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class ListUtilsTest {

    @Test
    public void testAddIfValueNotNull() {
        ArrayList<String> strings = new ArrayList<>();

        Assertions.assertTrue(ListUtils.addIfValueNotNull(strings, "foo"));
        Assertions.assertEquals("foo", strings.get(0));

        Assertions.assertFalse(ListUtils.addIfValueNotNull(strings, null));
    }

    @Test
    public void testAddAllIfAllValuesNotNull() {
        ArrayList<String> strings = new ArrayList<>();

        Assertions.assertTrue(
                ListUtils.addAllIfAllValuesNotNull(
                        strings, new String[]{"a", "b", "c"}));
        Assertions.assertEquals("a", strings.get(0));
        Assertions.assertEquals("b", strings.get(1));
        Assertions.assertEquals("c", strings.get(2));

        Assertions.assertFalse(ListUtils.addAllIfAllValuesNotNull(strings, null));
        Assertions.assertFalse(ListUtils
                .addAllIfAllValuesNotNull(strings, new String[]{null}));
    }

}
