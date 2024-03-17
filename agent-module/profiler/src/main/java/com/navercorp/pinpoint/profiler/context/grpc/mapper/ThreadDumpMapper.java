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
package com.navercorp.pinpoint.profiler.context.grpc.mapper;

import com.navercorp.pinpoint.grpc.trace.PMonitorInfo;
import com.navercorp.pinpoint.grpc.trace.PThreadDump;
import com.navercorp.pinpoint.grpc.trace.PThreadState;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.MonitorInfoMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.ThreadDumpMetricSnapshot;
import org.mapstruct.AfterMapping;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.EnumMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ValueMapping;

/**
 * @author intr3p1d
 */
@Mapper(
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {
        }
)
public interface ThreadDumpMapper {

    @Mappings({
            @Mapping(source = "threadState", target = "threadState"),
            @Mapping(source = "stackTrace", target = "stackTraceList", ignore = true),
            @Mapping(source = "lockedMonitors", target = "lockedMonitorList"),
            @Mapping(source = "lockedSynchronizers", target = "lockedSynchronizerList", ignore = true),
    })
    PThreadDump map(ThreadDumpMetricSnapshot snapshot);
    
    @AfterMapping
    default void addAll(ThreadDumpMetricSnapshot snapshot, @MappingTarget PThreadDump.Builder builder) {
        for (String stackTrace : snapshot.getStackTrace()) {
            builder.addStackTrace(stackTrace);
        }
        for (String lockedSynchronizer : snapshot.getLockedSynchronizers()) {
            builder.addLockedSynchronizer(lockedSynchronizer);
        }
    }

    @EnumMapping(nameTransformationStrategy = "prefix", configuration = "THREAD_STATE_")
    @ValueMapping(source = MappingConstants.ANY_REMAINING, target = "THREAD_STATE_UNKNOWN")
    PThreadState map(Thread.State state);

    PMonitorInfo map(MonitorInfoMetricSnapshot monitorInfoMetricSnapshot);

}
