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

package com.navercorp.pinpoint.web.vo;

import org.junit.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.web.vo.Range;

/**
 * @author emeroad
 */
public class RangeTest {
    @Test
    public void testCreate() {
        Range range1 = new Range(0, 0);
        Range range2 = new Range(0, 1);

        try {
            Range range3 = new Range(0, -1);
            Assert.fail();
        } catch (Exception ignored) {
        }

    }

    @Test
    public void testRange() {
        Range range1 =  new Range(0, 0);
        Assert.assertEquals(range1.getRange(), 0);

        Range range2 =  new Range(0, 1);
        Assert.assertEquals(range2.getRange(), 1);
    }
}
