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

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;


/**
 * @author Woonduk Kang(emeroad)
 */
public class CollectionUtilsTest {

    @Test
    public void nullSafeSize() throws Exception {
        Assert.assertEquals(CollectionUtils.nullSafeSize(Lists.newArrayList(123)), 1);

        Assert.assertEquals(CollectionUtils.nullSafeSize(Collections.emptyList()), 0);
        Assert.assertEquals(CollectionUtils.nullSafeSize(null), 0);
    }

    @Test
    public void nullSafeSize_nullValue() throws Exception {
        Assert.assertEquals(CollectionUtils.nullSafeSize(null, -1), -1);
    }

    @Test
    public void isEmpty() throws Exception {
        Assert.assertTrue(CollectionUtils.isEmpty(null));
        Assert.assertTrue(CollectionUtils.isEmpty(Collections.emptyList()));
    }

    @Test
    public void isNotEmpty() throws Exception {
        Assert.assertFalse(CollectionUtils.hasLength(Collections.emptyList()));
        Assert.assertFalse(CollectionUtils.hasLength(null));
    }

}