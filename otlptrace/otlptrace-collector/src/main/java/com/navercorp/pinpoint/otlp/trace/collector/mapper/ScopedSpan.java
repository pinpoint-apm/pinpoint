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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.trace.v1.Span;

/**
 * A Span paired with its owning {@link InstrumentationScope}. The scope→span association only
 * exists on the {@code ScopeSpans} container, so it must be captured when the containers are
 * flattened into per-trace span lists ({@code OtlpTraceMapper.getSpanMap}) — every later stage
 * (root/child classification, tree linking) sees spans detached from their container.
 */
record ScopedSpan(Span span, InstrumentationScope scope) {
}
