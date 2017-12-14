/*
 * Copyright 2016 Pinpoint contributors and NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jboss;

import com.navercorp.pinpoint.bootstrap.context.Trace;

/**
 * The Interface TraceAccessor.
 *
 */
public interface TraceAccessor {

    /**
     * $ PINPOIN t$ set trace.
     *
     * @param trace the trace
     */
    void _$PINPOINT$_setTrace(final Trace trace);

    /**
     * $ PINPOIN t$ get trace.
     *
     * @return the trace
     */
    Trace _$PINPOINT$_getTrace();
}