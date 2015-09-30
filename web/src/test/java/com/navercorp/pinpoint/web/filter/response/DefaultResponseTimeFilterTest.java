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

package com.navercorp.pinpoint.web.filter.response;

import com.navercorp.pinpoint.web.filter.responsetime.DefaultResponseTimeFilter;
import com.navercorp.pinpoint.web.filter.responsetime.ResponseTimeFilter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class DefaultResponseTimeFilterTest {

    @Test
    public void testAccept() throws Exception {

        ResponseTimeFilter filter1 = new DefaultResponseTimeFilter(1000, 2000);

        Assert.assertTrue(filter1.accept(1100));

        // between
        Assert.assertTrue(filter1.accept(1000));
        Assert.assertTrue(filter1.accept(2000));

        // lower
        Assert.assertFalse(filter1.accept(500));
        // upper
        Assert.assertFalse(filter1.accept(2500));

    }
}