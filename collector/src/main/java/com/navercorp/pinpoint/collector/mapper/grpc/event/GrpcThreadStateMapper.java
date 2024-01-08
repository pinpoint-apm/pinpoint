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

package com.navercorp.pinpoint.collector.mapper.grpc.event;

import com.navercorp.pinpoint.common.server.bo.event.ThreadState;
import com.navercorp.pinpoint.grpc.trace.PThreadState;
import org.springframework.stereotype.Component;

/**
 * @author jaehong.kim
 */
@Component
public class GrpcThreadStateMapper {

    public ThreadState map(final PThreadState threadState) {
        return switch (threadState) {
            case THREAD_STATE_NEW -> ThreadState.NEW;
            case THREAD_STATE_RUNNABLE -> ThreadState.RUNNABLE;
            case THREAD_STATE_BLOCKED -> ThreadState.BLOCKED;
            case THREAD_STATE_WAITING -> ThreadState.WAITING;
            case THREAD_STATE_TIMED_WAITING -> ThreadState.TIMED_WAITING;
            case THREAD_STATE_TERMINATED -> ThreadState.TERMINATED;
            case THREAD_STATE_UNKNOWN -> ThreadState.UNKNOWN;
            default -> ThreadState.UNKNOWN;
        };
    }
}