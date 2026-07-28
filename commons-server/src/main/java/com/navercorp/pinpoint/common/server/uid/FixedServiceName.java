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
 * Constant serviceName supplier for reserved serviceUids whose name is fixed by
 * definition (e.g. DEFAULT): no registry lookup, safe to share across rows.
 */
public final class FixedServiceName implements ServiceNameSupplier {

    /**
     * uid 0's name is a system constant, not a registry mapping — shared across all rows.
     */
    public static final ServiceNameSupplier DEFAULT =
            new FixedServiceName(ServiceUid.DEFAULT_SERVICE_UID_CODE, ServiceUid.DEFAULT_SERVICE_UID_NAME);

    private final int serviceUid;
    private final String serviceName;

    public FixedServiceName(int serviceUid, String serviceName) {
        this.serviceUid = serviceUid;
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
    }

    @Override
    public String get() {
        return serviceName;
    }

    @Override
    public String toString() {
        return serviceName + "/" + serviceUid;
    }
}