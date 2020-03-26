/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.collector.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.common.server.bo.event.DeadlockBo;
import com.navercorp.pinpoint.common.server.bo.event.ThreadDumpBo;
import com.navercorp.pinpoint.thrift.dto.TDeadlock;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
@Component
public class ThriftDeadlockBoMapper implements ThriftBoMapper<DeadlockBo, TDeadlock> {

    private final ThriftThreadDumpBoMapper threadDumpBoMapper;

    public ThriftDeadlockBoMapper(ThriftThreadDumpBoMapper threadDumpBoMapper) {
        this.threadDumpBoMapper = Objects.requireNonNull(threadDumpBoMapper, "threadDumpBoMapper");
    }

    public DeadlockBo map(final TDeadlock tDeadlock) {
        final DeadlockBo deadlockBo = new DeadlockBo();
        deadlockBo.setDeadlockedThreadCount(tDeadlock.getDeadlockedThreadCount());

        if (tDeadlock.isSetDeadlockedThreadList()) {
            final List<ThreadDumpBo> threadDumpBoList = new ArrayList<>();
            for (TThreadDump threadDump : tDeadlock.getDeadlockedThreadList()) {
                final ThreadDumpBo threadDumpBo = this.threadDumpBoMapper.map(threadDump);
                threadDumpBoList.add(threadDumpBo);
            }
            deadlockBo.setThreadDumpBoList(threadDumpBoList);
        }

        return deadlockBo;
    }
}