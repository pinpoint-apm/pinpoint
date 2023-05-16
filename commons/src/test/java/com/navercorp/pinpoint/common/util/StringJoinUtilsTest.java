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

package com.navercorp.pinpoint.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class StringJoinUtilsTest {

    @Test
    public void join1() {
        List<String> stringList = new ArrayList<>();
        stringList.add("abc");

        String join = StringJoinUtils.join(stringList, ",");
        Assertions.assertEquals(join, "abc");

    }

    @Test
    public void join2() {
        List<String> stringList = new ArrayList<>();
        stringList.add("abc");
        stringList.add("bcd");

        String join = StringJoinUtils.join(stringList, ",");
        Assertions.assertEquals(join, "abc,bcd");

    }

    @Test
    public void join3() {
        List<String> stringList = new ArrayList<>();
        stringList.add("abc");
        stringList.add("bcd");
        stringList.add("f");

        String join = StringJoinUtils.join(stringList, ",");
        Assertions.assertEquals(join, "abc,bcd,f");

    }
}