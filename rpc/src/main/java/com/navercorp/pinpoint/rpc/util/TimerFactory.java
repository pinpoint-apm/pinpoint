/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.util;

import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;

import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.ThreadNameDeterminer;

import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class TimerFactory {

    public static HashedWheelTimer createHashedWheelTimer(String threadName, long tickDuration, TimeUnit unit, int ticksPerWheel) {
        final PinpointThreadFactory threadFactory = new PinpointThreadFactory(threadName, true);
        return createHashedWheelTimer(threadFactory, tickDuration, unit, ticksPerWheel);
    }

    public static HashedWheelTimer createHashedWheelTimer(PinpointThreadFactory threadFactory, long tickDuration, TimeUnit unit, int ticksPerWheel) {
        return new HashedWheelTimer(threadFactory, ThreadNameDeterminer.CURRENT, tickDuration, unit, ticksPerWheel);
    }

}
