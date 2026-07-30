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
 * Resolves the serviceName at {@code create()} time and returns a constant supplier.
 * Use for call paths that always consume the serviceName: the registry lookup (and a
 * possible cache-miss load) happens at decode time instead of surfacing later at the
 * first {@code get()}, e.g. in the middle of response serialization. Prefer
 * {@link LazyServiceNameFactory} for scan paths that may never read the name.
 */
public class EagerServiceNameFactory implements ServiceNameFactory {

    private final ServiceNameResolver resolver;

    public EagerServiceNameFactory(ServiceNameResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    @Override
    public ServiceNameSupplier create(ServiceUid serviceUid) {
        if (serviceUid.getUid() == ServiceUid.DEFAULT_SERVICE_UID_CODE) {
            return ServiceNameSupplier.DEFAULT;
        }
        String serviceName = resolver.resolve(serviceUid);
        return new FixedServiceName(serviceUid.getUid(), serviceName);
    }
}
