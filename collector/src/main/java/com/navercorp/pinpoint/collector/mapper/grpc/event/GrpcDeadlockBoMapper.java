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

import com.navercorp.pinpoint.common.server.bo.event.DeadlockBo;
import com.navercorp.pinpoint.common.server.bo.event.ThreadDumpBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.grpc.trace.PDeadlock;
import com.navercorp.pinpoint.grpc.trace.PThreadDump;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
@Component
public class GrpcDeadlockBoMapper {

    private final GrpcThreadDumpBoMapper threadDumpBoMapper;

    public GrpcDeadlockBoMapper(GrpcThreadDumpBoMapper threadDumpBoMapper) {
        this.threadDumpBoMapper = threadDumpBoMapper;
    }

    public DeadlockBo map(final PDeadlock deadlock) {
        final DeadlockBo deadlockBo = new DeadlockBo();
        deadlockBo.setDeadlockedThreadCount(deadlock.getCount());

        final List<PThreadDump> threadDumpList = deadlock.getThreadDumpList();
        if (CollectionUtils.hasLength(threadDumpList)) {
            final List<ThreadDumpBo> threadDumpBoList = new ArrayList<>();
            for (PThreadDump threadDump : threadDumpList) {
                final ThreadDumpBo threadDumpBo = this.threadDumpBoMapper.map(threadDump);
                threadDumpBoList.add(threadDumpBo);
            }
            deadlockBo.setThreadDumpBoList(threadDumpBoList);
        }

        return deadlockBo;
    }
}