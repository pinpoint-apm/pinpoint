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


/**
 * Resolves a serviceUid restored from a UID-bearing span qualifier back to its
 * serviceName. The qualifier stores only the uid; the name lives in the service
 * registry, which is a read-side (web) concern — this port keeps the decoder free
 * of that dependency.
 */
@FunctionalInterface
public interface ServiceNameResolver {

    /**
     * Write-side / registry-less fallback: every uid maps to the DEFAULT name.
     */
    ServiceNameResolver FALLBACK = serviceUid -> ServiceUid.DEFAULT_SERVICE_UID_NAME;

    String resolve(ServiceUid serviceUid);
}
