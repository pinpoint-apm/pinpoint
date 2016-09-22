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

import com.lmax.disruptor.WaitStrategy;

/**
 * @author Taejin Koo
 */
public final class DisruptorUtils {

    public static int nextPowerOfTwo(int v) {
        return 1 << (32 - Integer.numberOfLeadingZeros(v - 1));
    }

    public static WaitStrategy createStrategy(String strategy) {
        return createStrategy(strategy, -1L);
    }

    public static WaitStrategy createStrategy(String strategy, long timeout) {
        DisruptorStrategyType type = DisruptorStrategyType.getValue(strategy);
        return type.create(timeout);
    }

}
