/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.common.server.util.concurrent;

import org.junit.Assert;
import org.junit.Test;

import com.lmax.disruptor.WaitStrategy;

/**
 * @author Taejin Koo
 */
public class DisruptorUtilsTest {

    @Test
    public void createStrategyTest() throws Exception {
        WaitStrategy strategy = DisruptorUtils.createStrategy(null);
        Assert.assertEquals(strategy.getClass(), DisruptorStrategyType.DEFAULT.create(-1L).getClass());

        strategy = DisruptorUtils.createStrategy("sleep");
        Assert.assertEquals(strategy.getClass(), DisruptorStrategyType.SLEEP.create(-1L).getClass());

        strategy = DisruptorUtils.createStrategy("Block");
        Assert.assertEquals(strategy.getClass(), DisruptorStrategyType.BLOCK.create(-1L).getClass());

        strategy = DisruptorUtils.createStrategy("lite_Block");
        Assert.assertEquals(strategy.getClass(), DisruptorStrategyType.LITE_BLOCK.create(-1L).getClass());

        strategy = DisruptorUtils.createStrategy("TIMEOUT_Block", 100L);
        Assert.assertEquals(strategy.getClass(), DisruptorStrategyType.TIMEOUT_BLOCK.create(100L).getClass());

        strategy = DisruptorUtils.createStrategy("YIELD", -1L);
        Assert.assertEquals(strategy.getClass(), DisruptorStrategyType.YIELD.create(1L).getClass());

        strategy = DisruptorUtils.createStrategy("BUSY_SPIN", -1L);
        Assert.assertEquals(strategy.getClass(), DisruptorStrategyType.BUSY_SPIN.create(1L).getClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionTest1() throws Exception {
        DisruptorUtils.createStrategy("ILLEGAL");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionTest2() throws Exception {
        DisruptorUtils.createStrategy("TIMEOUT_BLOCK");
    }

}
