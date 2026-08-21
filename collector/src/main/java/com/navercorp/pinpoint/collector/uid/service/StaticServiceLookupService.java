package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.concurrent.CompletableFuture;

/**
 * Fallback used when service lookup is disabled: resolves every serviceName to
 * {@link ServiceUid#DEFAULT} without touching storage.
 * Service lookup becomes mandatory in 4.0.0, remove this implementation then.
 */
public class StaticServiceLookupService implements ServiceLookupService {

    public ServiceUid staticServiceUid(String serviceName) {
        if (StringUtils.isEmpty(serviceName)) {
            return ServiceUid.DEFAULT;
        }
        if (ServiceUid.DEFAULT_SERVICE_UID_NAME.equals(serviceName)) {
            return ServiceUid.DEFAULT;
        }
        // TODO ServiceUid query
        return ServiceUid.DEFAULT;
    }

    @Override
    public CompletableFuture<ServiceUid> getServiceUid(String serviceName) {
        ServiceUid serviceUid = staticServiceUid(serviceName);
        // A new future per call: CompletableFuture is mutable (obtrudeValue/cancel), so a shared
        // constant would leak state changes across callers.
        return CompletableFuture.completedFuture(serviceUid);
    }
}
