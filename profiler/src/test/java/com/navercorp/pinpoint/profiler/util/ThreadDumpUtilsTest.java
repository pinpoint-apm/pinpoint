/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.profiler.context.thrift.ThreadStateThriftMessageConverter;
import com.navercorp.pinpoint.thrift.dto.command.TThreadState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ThreadDumpUtilsTest {

    private final Logger logger = LogManager.getLogger(this.getClass());


    @Test
    public void toTThreadState() {
        ThreadStateThriftMessageConverter threadStateThriftMessageConverter = new ThreadStateThriftMessageConverter();

        TThreadState newState = threadStateThriftMessageConverter.toMessage(Thread.State.NEW);
        Assertions.assertEquals(newState, TThreadState.NEW);

        TThreadState runnable = threadStateThriftMessageConverter.toMessage(Thread.State.RUNNABLE);
        Assertions.assertEquals(runnable, TThreadState.RUNNABLE);

        TThreadState blocked = threadStateThriftMessageConverter.toMessage(Thread.State.BLOCKED);
        Assertions.assertEquals(blocked, TThreadState.BLOCKED);


        TThreadState waiting = threadStateThriftMessageConverter.toMessage(Thread.State.WAITING);
        Assertions.assertEquals(waiting, TThreadState.WAITING);

        TThreadState timedWaiting = threadStateThriftMessageConverter.toMessage(Thread.State.TIMED_WAITING);
        Assertions.assertEquals(timedWaiting, TThreadState.TIMED_WAITING);

        TThreadState terminated = threadStateThriftMessageConverter.toMessage(Thread.State.TERMINATED);
        Assertions.assertEquals(terminated, TThreadState.TERMINATED);
    }
}