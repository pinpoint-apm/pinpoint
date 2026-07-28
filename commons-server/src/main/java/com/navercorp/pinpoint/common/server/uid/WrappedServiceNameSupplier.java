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

package com.navercorp.pinpoint.common.server.uid;

import java.util.Objects;

/**
 * Wraps an already-resolved serviceName (e.g. from the write-path {@code ServerHeader})
 * where no serviceUid is available — unlike {@link FixedServiceName}, which pairs the
 * name with its uid. A named type (instead of a lambda) so debuggers, stack traces and
 * {@code SpanOwner.toString()} show the captured state.
 */
public final class WrappedServiceNameSupplier implements ServiceNameSupplier {

    private final String serviceName;

    public WrappedServiceNameSupplier(String serviceName) {
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
    }

    @Override
    public String get() {
        return serviceName;
    }

    @Override
    public String toString() {
        return serviceName;
    }
}
