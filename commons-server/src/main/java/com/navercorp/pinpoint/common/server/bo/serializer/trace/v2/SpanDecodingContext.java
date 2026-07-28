/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.buffer.StringAllocator;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.common.server.uid.FixedServiceName;
import com.navercorp.pinpoint.common.server.uid.ServiceNameFactory;
import com.navercorp.pinpoint.common.server.uid.ServiceNameSupplier;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanDecodingContext {

    private final ServerTraceId transactionId;

    //    private AnnotationBo prevAnnotationBo;
    private long collectorAcceptedTime;

    private StringAllocator stringAllocator = StringAllocator.DEFAULT_ALLOCATOR;

    private ServiceNameFactory serviceNameFactory = ServiceNameFactory.FALLBACK;

    // Row-local cache: one serviceName supplier per distinct serviceUid (primitive int key,
    // no boxing), shared by every span/chunk of this row. Lazily allocated — legacy (non-UID)
    // rows never touch it.
    // Row-scoped state: unlike collectorAcceptedTime, this survives next() on purpose.
    private MutableIntObjectMap<ServiceNameSupplier> serviceNameCache;

    public SpanDecodingContext(ServerTraceId transactionId) {
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
    }

    public void setServiceNameFactory(ServiceNameFactory serviceNameFactory) {
        this.serviceNameFactory = Objects.requireNonNull(serviceNameFactory, "serviceNameFactory");
    }

    public ServiceNameSupplier getServiceName(ServiceUid serviceUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");

        if (serviceUid.getUid() == ServiceUid.DEFAULT_SERVICE_UID_CODE) {
            // allocation fast path (the factory returns the same constant):
            // DEFAULT-only rows never allocate the cache map
            return FixedServiceName.DEFAULT;
        }

        MutableIntObjectMap<ServiceNameSupplier> cache = this.serviceNameCache;
        if (cache == null) {
            cache = IntObjectMaps.mutable.of();
            this.serviceNameCache = cache;
        }
        return cache.getIfAbsentPutWith(serviceUid.getUid(), serviceNameFactory::create, serviceUid);
    }

//    public AnnotationBo getPrevFirstAnnotationBo() {
//        return prevAnnotationBo;
//    }
//
//    public void setPrevFirstAnnotationBo(AnnotationBo prevAnnotationBo) {
//        this.prevAnnotationBo = prevAnnotationBo;
//    }

    public void setCollectorAcceptedTime(long collectorAcceptedTime) {
        this.collectorAcceptedTime = collectorAcceptedTime;
    }

    public long getCollectorAcceptedTime() {
        return collectorAcceptedTime;
    }

    public ServerTraceId getTransactionId() {
        return transactionId;
    }

    public String encoding(byte[] bytes) {
        return stringAllocator.allocate(bytes, 0, bytes.length, StandardCharsets.UTF_8);
    }

    public void setStringAllocator(StringAllocator stringAllocator) {
        this.stringAllocator = Objects.requireNonNull(stringAllocator, "stringAllocator");
    }

    public void next() {
    }

    public void finish() {
    }
}
