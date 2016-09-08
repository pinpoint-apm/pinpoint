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

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;

/**
 * @author Taejin Koo
 */
public enum DisruptorStrategyType {

    SLEEP() {
        @Override
        WaitStrategy create(long timeout) {
            return new SleepingWaitStrategy();
        }
    },
    BLOCK {
        @Override
        WaitStrategy create(long timeout) {
            return new BlockingWaitStrategy();
        }
    },
    LITE_BLOCK {
        @Override
        WaitStrategy create(long timeout) {
            return new LiteBlockingWaitStrategy();
        }
    },
    TIMEOUT_BLOCK {
        @Override
        WaitStrategy create(long timeout) {
            if (timeout <= 0) {
                throw new IllegalArgumentException("timeout must be greater than 0");
            }
            return new TimeoutBlockingWaitStrategy(timeout, TimeUnit.MILLISECONDS);
        }
    },
    YIELD {
        @Override
        WaitStrategy create(long timeout) {
            return new YieldingWaitStrategy();
        }
    },
    BUSY_SPIN {
        @Override
        WaitStrategy create(long timeout) {
            return new BusySpinWaitStrategy();
        }
    },
    DEFAULT {
        @Override
        WaitStrategy create(long timeout) {
            return LITE_BLOCK.create(timeout);
        }
    };

    private DisruptorStrategyType() {
    }

    static DisruptorStrategyType getValue(String strategy) {
        if (StringUtils.isEmpty(strategy)) {
            return DEFAULT;
        }

        for (DisruptorStrategyType type : values()) {
            if (type.name().equalsIgnoreCase(strategy)) {
                return type;
            }
        }

        throw new IllegalArgumentException(strategy + " not matched");
    }

    abstract WaitStrategy create(long timeout);

}