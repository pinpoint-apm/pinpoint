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
 * Lazy serviceName supplier for a serviceUid restored from a UID-bearing span
 * qualifier: the registry is consulted only when {@code getServiceName()} is
 * actually called. A named type (instead of a lambda) so debuggers, stack traces
 * and {@code SpanOwner.toString()} show the captured state.
 */
public final class LazyServiceName implements ServiceNameSupplier {
    private final ServiceNameResolver resolver;
    private final ServiceUid serviceUid;

    // Memoized result. This instance is shared row-wide via SpanDecodingContext's
    // serviceUid cache, so the registry is consulted at most once per row per uid.
    // Benign race: a concurrent first get() may resolve twice with an identical result.
    private String resolved;

    public LazyServiceName(ServiceNameResolver resolver, ServiceUid serviceUid) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.serviceUid = Objects.requireNonNull(serviceUid, "serviceUid");
    }

    @Override
    public String get() {
        String name = this.resolved;
        if (name == null) {
            name = resolver.resolve(serviceUid);
            this.resolved = name;
        }
        return name;
    }

    public ServiceUid serviceUid() {
        return serviceUid;
    }

    @Override
    public String toString() {
        // report the memoized name if already resolved, but never trigger resolution here —
        // debuggers and log statements must not cause a registry lookup
        final String name = this.resolved;
        if (name != null) {
            return name + '/' + serviceUid;
        }
        return "?/" + serviceUid;
    }

}
