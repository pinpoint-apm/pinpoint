/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CollectionUtilsTest {

    @Test
    public void nullSafeSize() {
        Assertions.assertEquals(1, CollectionUtils.nullSafeSize(Collections.singletonList(123)));

        Assertions.assertEquals(0, CollectionUtils.nullSafeSize(Collections.emptyList()));
        Assertions.assertEquals(0, CollectionUtils.nullSafeSize(null));
    }

    @Test
    public void nullSafeSize_nullValue() {
        Assertions.assertEquals(-1, CollectionUtils.nullSafeSize(null, -1));
    }

    @Test
    public void isEmpty() {
        Assertions.assertTrue(CollectionUtils.isEmpty(null));
        Assertions.assertTrue(CollectionUtils.isEmpty(Collections.emptyList()));
    }

    @Test
    public void isNotEmpty() {
        Assertions.assertFalse(CollectionUtils.hasLength(null));
        Assertions.assertFalse(CollectionUtils.hasLength(Collections.emptyList()));
    }

}