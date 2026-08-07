package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.navercorp.pinpoint.common.server.uid.ServiceUidSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceUidSuppliersTest {

    @Test
    void fetchOnCreate() {
        AtomicReference<String> requestedServiceName = new AtomicReference<>();
        UidFetcher uidFetcher = serviceName -> {
            requestedServiceName.set(serviceName);
            return CompletableFuture.completedFuture(ServiceUid.of(100001));
        };

        ServiceUidSupplier supplier = ServiceUidSuppliers.newSupplier("serviceName", uidFetcher);

        assertThat(requestedServiceName).hasValue("serviceName");
        assertThat(supplier.get()).isEqualTo(ServiceUid.of(100001));
    }

    @Test
    void throwExceptionOnTimeout() {
        UidFetcher uidFetcher = serviceName -> new CompletableFuture<>();

        ServiceUidSupplier supplier = ServiceUidSuppliers.newSupplier("serviceName", uidFetcher, 1, TimeUnit.MILLISECONDS);

        assertThatThrownBy(supplier::get).isInstanceOf(UidException.class);
    }

    @Test
    void throwExceptionOnMissingServiceUid() {
        UidFetcher uidFetcher = serviceName -> CompletableFuture.completedFuture(null);

        ServiceUidSupplier supplier = ServiceUidSuppliers.newSupplier("serviceName", uidFetcher);

        assertThatThrownBy(supplier::get)
                .isInstanceOf(UidNotFoundException.class)
                .hasMessageContaining("serviceName");
    }

    @Test
    void throwExceptionOnGetWhenFetcherException() {
        UidFetcher uidFetcher = serviceName -> {
            throw new RuntimeException("error");
        };

        ServiceUidSupplier supplier = ServiceUidSuppliers.newSupplier("serviceName", uidFetcher);

        assertThatThrownBy(supplier::get).isInstanceOf(UidException.class);
    }
}
