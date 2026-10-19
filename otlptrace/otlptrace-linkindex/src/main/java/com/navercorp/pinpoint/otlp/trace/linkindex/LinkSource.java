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

package com.navercorp.pinpoint.otlp.trace.linkindex;

/**
 * InLink subLink written by the join job. Each (minute, subLink) row is written by exactly one
 * job run, so the job can put absolute values and stay idempotent on re-run.
 * <ul>
 *     <li>{@link #SAME_SLOT}: the SERVER entry was in the slot being joined; written to that slot's minute.</li>
 *     <li>{@link #CROSS_SLOT}: the SERVER entry was in the previous slot; written to the previous minute.</li>
 * </ul>
 * The collector never writes a subLink starting with {@link #RESERVED_PREFIX}: its subLinks are the
 * caller endPoint (host:port) or {@code "_"}.
 */
public enum LinkSource {
    SAME_SLOT("_otel"),
    CROSS_SLOT("_otelx");

    public static final String RESERVED_PREFIX = "_otel";

    private final String subLink;

    LinkSource(String subLink) {
        this.subLink = subLink;
    }

    public String getSubLink() {
        return subLink;
    }
}
