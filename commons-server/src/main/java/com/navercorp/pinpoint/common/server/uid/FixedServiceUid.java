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
 * Constant serviceUid supplier restored from a UID-bearing span qualifier.
 * A named type (instead of a lambda) so debuggers, stack traces and
 * {@code SpanOwner.toString()} show the captured state.
 */
public final class FixedServiceUid implements ServiceUidSupplier {

    /**
     * Shared constant for the DEFAULT serviceUid.
     */
    public static final ServiceUidSupplier DEFAULT = new FixedServiceUid(ServiceUid.DEFAULT);

    private final ServiceUid serviceUid;

    public FixedServiceUid(ServiceUid serviceUid) {
        this.serviceUid = Objects.requireNonNull(serviceUid, "serviceUid");
    }

    @Override
    public ServiceUid get() {
        return serviceUid;
    }

    public ServiceUid serviceUid() {
        return serviceUid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        FixedServiceUid that = (FixedServiceUid) obj;
        return this.serviceUid.getUid() == that.serviceUid.getUid();
    }

    @Override
    public int hashCode() {
        return serviceUid.hashCode();
    }

    @Override
    public String toString() {
        return Integer.toString(serviceUid.getUid());
    }

}
