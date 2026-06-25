package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

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

        Supplier<ServiceUid> supplier = ServiceUidSuppliers.newSupplier("serviceName", uidFetcher);

        assertThat(requestedServiceName).hasValue("serviceName");
        assertThat(supplier.get()).isEqualTo(ServiceUid.of(100001));
    }

    @Test
    void getDefaultOnEmptyServiceName() {
        UidFetcher uidFetcher = serviceName -> {
            throw new AssertionError("should not fetch");
        };

        Supplier<ServiceUid> supplier = ServiceUidSuppliers.newSupplier("", uidFetcher);

        assertThat(supplier.get()).isEqualTo(ServiceUid.DEFAULT);
    }

    @Test
    void getDefaultOnDefaultServiceName() {
        UidFetcher uidFetcher = serviceName -> {
            throw new AssertionError("should not fetch");
        };

        Supplier<ServiceUid> supplier = ServiceUidSuppliers.newSupplier(ServiceUid.DEFAULT_SERVICE_UID_NAME, uidFetcher);

        assertThat(supplier.get()).isEqualTo(ServiceUid.DEFAULT);
    }

    @Test
    void throwExceptionOnTimeout() {
        UidFetcher uidFetcher = serviceName -> new CompletableFuture<>();

        Supplier<ServiceUid> supplier = ServiceUidSuppliers.newSupplier("serviceName", uidFetcher, 1, TimeUnit.MILLISECONDS);

        assertThatThrownBy(supplier::get).isInstanceOf(UidException.class);
    }

    @Test
    void returnNullOnMissingServiceUid() {
        UidFetcher uidFetcher = serviceName -> CompletableFuture.completedFuture(null);

        Supplier<ServiceUid> supplier = ServiceUidSuppliers.newSupplier("serviceName", uidFetcher);

        assertThat(supplier.get()).isNull();
    }

    @Test
    void throwExceptionOnGetWhenFetcherException() {
        UidFetcher uidFetcher = serviceName -> {
            throw new RuntimeException("error");
        };

        Supplier<ServiceUid> supplier = ServiceUidSuppliers.newSupplier("serviceName", uidFetcher);

        assertThatThrownBy(supplier::get).isInstanceOf(UidException.class);
    }
}
