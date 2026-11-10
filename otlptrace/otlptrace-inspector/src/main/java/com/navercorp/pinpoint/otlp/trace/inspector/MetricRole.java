/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.trace.inspector;

/**
 * What a metric means to the Inspector, independent of the naming dialect that produced it. A
 * {@link MetricDialect} maps metric names to roles; the mapper only knows roles.
 */
public enum MetricRole {
    /** Bytes in use per memory pool, with a heap/non-heap type attribute. */
    MEMORY_USED,
    /** Maximum bytes per memory pool; absent (OTel) or negative (Micrometer) for pools without a limit. */
    MEMORY_LIMIT,
    /** GC pause histogram per collector name. */
    GC_DURATION,
    /**
     * Completed GC cycles, a running total (Go {@code go.memory.gc.cycles}): the old GC count of a runtime
     * that reports its cycles and its pause time as two metrics.
     */
    GC_CYCLES,
    /**
     * GC pause time without the cycle count: a pause histogram whose sum is the GC time and whose count
     * is not the cycles (Go {@code go.memory.gc.pause.duration}: two pauses per cycle), or a running total
     * of pause time (.NET {@code dotnet.gc.pause.time}). The count comes from {@link #GC_CYCLES}.
     */
    GC_PAUSE_TIME,
    /** Process CPU utilization, 0..1. */
    CPU_JVM,
    /** Whole-system CPU utilization, 0..1 (experimental in the OTel Java agent, default in Micrometer). */
    CPU_SYSTEM,
    /**
     * CPU seconds used by the process, a running total split by CPU mode ({@code process.cpu.time}).
     * With {@link #SYSTEM_CPU_TIME} it gives the process CPU utilization of the whole machine.
     */
    PROCESS_CPU_TIME,
    /** CPU seconds of the whole machine, a running total split by CPU mode, idle included ({@code system.cpu.time}). */
    SYSTEM_CPU_TIME,
    /**
     * CPU seconds of a language runtime's own capacity, idle included (Go {@code go.cpu.time}: GOMAXPROCS
     * times the wall time); used for the process CPU when {@link #PROCESS_CPU_TIME} is absent.
     */
    RUNTIME_CPU_TIME,
    /**
     * CPUs the process can use (.NET {@code dotnet.process.cpu.count}): with {@link #PROCESS_CPU_TIME}
     * and the elapsed time it gives the process CPU when {@link #SYSTEM_CPU_TIME} is absent.
     */
    CPU_COUNT,
    /** Live threads, possibly split by state/daemon attributes. */
    THREAD_COUNT,
    /** Currently loaded classes. */
    CLASS_COUNT,
    /** Classes unloaded since start (running total). */
    CLASS_UNLOADED,
    /** Buffers per pool (direct/mapped). */
    BUFFER_COUNT,
    /** Buffer memory per pool (direct/mapped). */
    BUFFER_MEMORY_USED,
    /** Open file descriptors (experimental in the OTel Java agent, default in Micrometer). */
    FILE_DESCRIPTOR,
    /** Server request duration histogram; all attribute series are folded together. */
    HTTP_SERVER_DURATION,
    /**
     * Connections in use per connection pool: the OTel pool count with the "used" state, or a
     * dialect's own active-connections metric.
     */
    CONNECTION_POOL_USED,
    /** Maximum connections per connection pool. */
    CONNECTION_POOL_MAX,
    /**
     * Process start time as a point value (Micrometer {@code process.start.time}). Not a chart: it
     * feeds the agent's session start time, which OTel otherwise only carries as the
     * {@code process.creation.time} resource attribute.
     */
    START_TIME
}
