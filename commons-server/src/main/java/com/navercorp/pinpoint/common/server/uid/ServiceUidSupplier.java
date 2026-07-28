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

import java.util.function.Supplier;

/**
 * Explicit supplier type for {@link ServiceUid}: a plain {@code Supplier<ServiceUid>}
 * is erased at runtime and invisible to type-based search — this interface makes the
 * serviceUid flow traceable across module boundaries.
 */
@FunctionalInterface
public interface ServiceUidSupplier extends Supplier<ServiceUid> {

    /**
     * Shared constant for the DEFAULT serviceUid.
     */
    ServiceUidSupplier DEFAULT = new FixedServiceUid(ServiceUid.DEFAULT);
}
