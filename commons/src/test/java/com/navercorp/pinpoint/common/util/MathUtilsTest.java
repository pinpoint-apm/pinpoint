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

package com.navercorp.pinpoint.common.util;

import org.junit.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.util.MathUtils;

/**
 * @author emeroad
 */
public class MathUtilsTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Test
    public void fastAbs() {
        Assert.assertTrue(MathUtils.fastAbs(-1) > 0);
        Assert.assertTrue(MathUtils.fastAbs(0) == 0);
        Assert.assertTrue(MathUtils.fastAbs(1) > 0);
    }

    @Test
    public void overflow() {

        logger.debug("abs:{}", Math.abs(Integer.MIN_VALUE));
        logger.debug("fastabs:{}", MathUtils.fastAbs(Integer.MIN_VALUE));

        int index = Integer.MIN_VALUE - 2;
        for (int i = 0; i < 5; i++) {
            logger.debug("{}------------", i);
            logger.debug("{}", index);
            logger.debug("mod:{}", index % 3);
            logger.debug("abs:{}", Math.abs(index));
            logger.debug("fastabs:{}", MathUtils.fastAbs(index));

            index++;
        }
    }
    
    @Test
    public void roundToNearestMultipleOf() {
        Assert.assertEquals(1, MathUtils.roundToNearestMultipleOf(1, 1));
        Assert.assertEquals(4, MathUtils.roundToNearestMultipleOf(1, 4));
        Assert.assertEquals(4, MathUtils.roundToNearestMultipleOf(2, 4));
        Assert.assertEquals(4, MathUtils.roundToNearestMultipleOf(3, 4));
        Assert.assertEquals(4, MathUtils.roundToNearestMultipleOf(4, 4));
        Assert.assertEquals(4, MathUtils.roundToNearestMultipleOf(5, 4));
        Assert.assertEquals(8, MathUtils.roundToNearestMultipleOf(6, 4));
        Assert.assertEquals(8, MathUtils.roundToNearestMultipleOf(7, 4));
        Assert.assertEquals(8, MathUtils.roundToNearestMultipleOf(8, 4));
        Assert.assertEquals(10, MathUtils.roundToNearestMultipleOf(10, 5));
        Assert.assertEquals(10, MathUtils.roundToNearestMultipleOf(11, 5));
        Assert.assertEquals(10, MathUtils.roundToNearestMultipleOf(12, 5));
        Assert.assertEquals(15, MathUtils.roundToNearestMultipleOf(13, 5));
        Assert.assertEquals(15, MathUtils.roundToNearestMultipleOf(14, 5));
        Assert.assertEquals(15, MathUtils.roundToNearestMultipleOf(15, 5));
        Assert.assertEquals(15, MathUtils.roundToNearestMultipleOf(16, 5));
        Assert.assertEquals(15, MathUtils.roundToNearestMultipleOf(17, 5));
        Assert.assertEquals(20, MathUtils.roundToNearestMultipleOf(18, 5));
        Assert.assertEquals(20, MathUtils.roundToNearestMultipleOf(19, 5));
        Assert.assertEquals(20, MathUtils.roundToNearestMultipleOf(20, 5));
        Assert.assertEquals(5000, MathUtils.roundToNearestMultipleOf(6000, 5000));
        Assert.assertEquals(10000, MathUtils.roundToNearestMultipleOf(9000, 5000));
    }

}
