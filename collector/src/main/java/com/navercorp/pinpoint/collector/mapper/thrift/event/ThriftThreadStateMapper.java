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

package com.navercorp.pinpoint.collector.mapper.thrift.event;

import com.navercorp.pinpoint.common.server.bo.event.ThreadState;
import com.navercorp.pinpoint.thrift.dto.command.TThreadState;
import org.springframework.stereotype.Component;


/**
 * @author jaehong.kim
 */
@Component
public class ThriftThreadStateMapper {

    public ThreadState map(final TThreadState threadState) {
        switch (threadState) {
            case NEW:
                return ThreadState.NEW;
            case RUNNABLE:
                return ThreadState.RUNNABLE;
            case BLOCKED:
                return ThreadState.BLOCKED;
            case WAITING:
                return ThreadState.WAITING;
            case TIMED_WAITING:
                return ThreadState.TIMED_WAITING;
            case TERMINATED:
                return ThreadState.TERMINATED;
            case UNKNOWN:
                return ThreadState.UNKNOWN;
            default:
                return ThreadState.UNKNOWN;
        }
    }
}