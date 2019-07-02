/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.thrift;

import com.navercorp.pinpoint.thrift.dto.command.TThreadState;

/**
 * @author jaehong.kim
 */
public class ThreadStateThriftMessageConverter implements MessageConverter<TThreadState> {
    @Override
    public TThreadState toMessage(Object message) {
        if (message instanceof Thread.State) {
            final Thread.State threadState = (Thread.State) message;
            return convertThreadState(threadState);
        } else {
            throw new IllegalArgumentException("invalid message type. message=" + message);
        }
    }

    private TThreadState convertThreadState(Thread.State threadState) {
        switch (threadState) {
            case NEW:
                return TThreadState.NEW;
            case RUNNABLE:
                return TThreadState.RUNNABLE;
            case BLOCKED:
                return TThreadState.BLOCKED;
            case WAITING:
                return TThreadState.WAITING;
            case TIMED_WAITING:
                return TThreadState.TIMED_WAITING;
            case TERMINATED:
                return TThreadState.TERMINATED;
        }
        return TThreadState.UNKNOWN;
    }
}