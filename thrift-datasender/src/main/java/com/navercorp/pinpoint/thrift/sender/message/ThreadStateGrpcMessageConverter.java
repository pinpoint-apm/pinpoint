/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.thrift.sender.message;

import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.grpc.trace.PThreadState;

/**
 * @author youngjin.kim2
 */
public class ThreadStateGrpcMessageConverter implements MessageConverter<Object, PThreadState> {

    @Override
    public PThreadState toMessage(Object message) {
        if (message instanceof Thread.State) {
            return toMessage((Thread.State) message);
        } else {
            throw new IllegalArgumentException("invalid message type. message=" + message);
        }
    }

    private PThreadState toMessage(Thread.State threadState) {
        switch (threadState) {
            case NEW:
                return PThreadState.THREAD_STATE_NEW;
            case RUNNABLE:
                return PThreadState.THREAD_STATE_RUNNABLE;
            case BLOCKED:
                return PThreadState.THREAD_STATE_BLOCKED;
            case WAITING:
                return PThreadState.THREAD_STATE_WAITING;
            case TIMED_WAITING:
                return PThreadState.THREAD_STATE_TIMED_WAITING;
            case TERMINATED:
                return PThreadState.THREAD_STATE_TERMINATED;
        }
        return PThreadState.THREAD_STATE_UNKNOWN;
    }

}
