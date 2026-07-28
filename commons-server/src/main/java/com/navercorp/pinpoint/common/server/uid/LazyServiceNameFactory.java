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
 * Creates {@link LazyServiceName} suppliers bound to this factory's
 * {@link ServiceNameResolver}. Owning the resolver here keeps the decoder's
 * per-span code free of supplier wiring.
 */
public class LazyServiceNameFactory implements ServiceNameFactory {

    private final ServiceNameResolver resolver;

    public LazyServiceNameFactory(ServiceNameResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    @Override
    public ServiceNameSupplier create(ServiceUid serviceUid) {
        if (serviceUid.getUid() == ServiceUid.DEFAULT_SERVICE_UID_CODE) {
            return ServiceNameSupplier.DEFAULT;
        }
        return new LazyServiceName(resolver, serviceUid);
    }
}
