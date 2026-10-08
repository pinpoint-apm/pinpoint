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

import com.navercorp.pinpoint.collector.uid.service.ServiceLookupService;
import com.navercorp.pinpoint.common.server.uid.FixedServiceUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUidSupplier;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.io.request.ServiceUidSuppliers;
import com.navercorp.pinpoint.io.request.UidNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Resolves the Pinpoint identity of an OTLP {@code Resource} — serviceName, applicationName,
 * agentId, agentName and the <em>serviceUid</em> — in one place, so the trace receiver
 * ({@code OtlpTraceMapper}) and the log receiver ({@code OtlpLogExportService}) agree on every
 * identifier and on the service policy.
 *
 * <p>Name resolution is {@link OtlpTraceMapperUtils#getId(Map, boolean)}. The serviceUid then
 * follows the native agent's V4 header path: the serviceName is looked up through
 * {@link ServiceLookupService} (MySQL service registry behind a cache, or the static DEFAULT
 * fallback when {@code pinpoint.collector.service.lookup.enabled=false}). Pinpoint's built-in
 * DEFAULT service is short-circuited without a lookup.</p>
 *
 * <p>A serviceName that is set explicitly but not registered is rejected with
 * {@link OtlpServiceNotFoundException} rather than silently folded into DEFAULT or stored under
 * an UNKNOWN uid: both would hide the data from the web, which queries by the selected service.
 * Lookup failures other than "not found" (timeout, registry error) propagate as
 * {@link com.navercorp.pinpoint.io.request.UidException} so the caller can report them as a
 * server-side fault.</p>
 */
@Component
public class OtlpResourceIdResolver {

    private final ServiceLookupService serviceLookupService;
    private final boolean allowApplicationNameFallback;

    public OtlpResourceIdResolver(ServiceLookupService serviceLookupService,
                                  @Value("${pinpoint.collector.otlptrace.application-name-fallback.enabled:false}") boolean allowApplicationNameFallback) {
        this.serviceLookupService = Objects.requireNonNull(serviceLookupService, "serviceLookupService");
        this.allowApplicationNameFallback = allowApplicationNameFallback;
    }

    /**
     * @throws IllegalArgumentException     when an identifier is missing or fails validation
     *                                      (client-side data fault)
     * @throws OtlpServiceNotFoundException when {@code pinpoint.serviceName} is not registered
     * @throws com.navercorp.pinpoint.io.request.UidException when the lookup itself fails
     */
    public IdAndName resolve(Map<String, AttributeValue> resourceAttributes) {
        final IdAndName idAndName = OtlpTraceMapperUtils.getId(resourceAttributes, allowApplicationNameFallback);
        final ServiceUidSupplier serviceUid = resolveServiceUid(idAndName.serviceName());
        return idAndName.withServiceUid(serviceUid);
    }

    private ServiceUidSupplier resolveServiceUid(String serviceName) {
        if (ServiceUid.DEFAULT_SERVICE_UID_NAME.equals(serviceName)) {
            return ServiceUidSupplier.DEFAULT;
        }
        final ServiceUidSupplier supplier = ServiceUidSuppliers.newSupplier(serviceName, serviceLookupService::getServiceUid);
        final ServiceUid serviceUid;
        try {
            // Resolve eagerly: the reject decision must be made while the ResourceSpans/ResourceLogs
            // is still in hand, and the lookup is cached (CachingServiceLookupService) so the cost
            // is one cache hit per Resource block, not per span.
            serviceUid = supplier.get();
        } catch (UidNotFoundException e) {
            throw new OtlpServiceNotFoundException(serviceName);
        }
        return new FixedServiceUid(serviceUid);
    }
}
