package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.common.server.io.MessageTypes;
import com.navercorp.pinpoint.common.server.io.ServerRequest;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderV4;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import com.navercorp.pinpoint.io.request.UidFetcher;
import io.grpc.Context;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DefaultServerRequestFactoryTest {

    @Test
    void getServiceUidFromUidFetcherForSpan() {
        assertServiceUidFromUidFetcher(MessageTypes.SPAN);
    }

    @Test
    void getServiceUidFromUidFetcherForStat() {
        assertServiceUidFromUidFetcher(MessageTypes.AGENT_STAT);
    }

    @Test
    void getServiceUidFromDefaultUidFetcherService() {
        AtomicReference<String> requestedServiceName = new AtomicReference<>();
        CompletableFuture<ServiceUid> future = new CompletableFuture<>();
        UidFetcher uidFetcher = serviceName -> {
            requestedServiceName.set(serviceName);
            return future;
        };
        DefaultServerRequestFactory factory = new DefaultServerRequestFactory(() -> uidFetcher);

        ServerRequest<String> request = factory.newServerRequest(newContext("serviceName"), MessageTypes.AGENT_INFO, "data");

        assertThat(requestedServiceName).hasValue("serviceName");

        future.complete(ServiceUid.of(100001));
        assertThat(request.getHeader().getServiceUid().get()).isEqualTo(ServiceUid.of(100001));
    }

    private void assertServiceUidFromUidFetcher(MessageTypes messageType) {
        DefaultServerRequestFactory factory = new DefaultServerRequestFactory();
        AtomicReference<String> requestedServiceName = new AtomicReference<>();
        CompletableFuture<ServiceUid> future = new CompletableFuture<>();
        UidFetcher uidFetcher = serviceName -> {
            requestedServiceName.set(serviceName);
            return future;
        };

        ServerRequest<String> request = factory.newServerRequest(newContext("serviceName"), uidFetcher, messageType, "data");

        assertThat(requestedServiceName).hasValue("serviceName");

        future.complete(ServiceUid.of(100001));
        assertThat(request.getHeader().getServiceUid().get()).isEqualTo(ServiceUid.of(100001));
    }

    private Context newContext(String serviceName) {
        Header header = new HeaderV4(
                "name",
                "agentId",
                "agentName",
                "applicationName",
                serviceName,
                null,
                1000,
                1,
                Header.SOCKET_ID_NOT_EXIST,
                Collections.emptyList(),
                Header.DEFAULT_GRPC_BUILT_IN_RETRY,
                Collections.emptyMap());
        return Context.current()
                .withValue(ServerContext.AGENT_INFO_KEY, header)
                .withValue(ServerContext.getTransportMetadataKey(), mock(TransportMetadata.class));
    }
}
