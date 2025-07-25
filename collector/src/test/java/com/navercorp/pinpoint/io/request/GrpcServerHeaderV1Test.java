package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.collector.receiver.grpc.cache.SingleEntryUidCacheV1;
import com.navercorp.pinpoint.collector.receiver.grpc.cache.UidCache;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderV1;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GrpcServerHeaderV1Test {

    @Test
    void getApplicationUid_cache_miss() {

        Header header = new HeaderV1("headername", "agentId", "agentName", "applicationName", 1, 0L, 0, List.of(), false, Map.of());
        UidCache cacheV1 = new SingleEntryUidCacheV1();
        ApplicationUidService service = mock(ApplicationUidService.class);
        when(service.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, "applicationName"))
                .thenReturn(CompletableFuture.completedFuture(ApplicationUid.of(100)));
        UidFetcher uidFetcher = new UidFetcherV1(service, cacheV1);

        GrpcServerHeaderV1 serverHeader = new GrpcServerHeaderV1(header, uidFetcher);

        Supplier<ApplicationUid> applicationUid = serverHeader.getApplicationUid();
        assertEquals(ApplicationUid.of(100), applicationUid.get());
    }


    @Test
    void getApplicationUid_cache_hit() {

        Header header = new HeaderV1("headername", "agentId", "agentName", "applicationName", 1, 0L, 0, List.of(), false, Map.of());
        UidCache cacheV1 = new SingleEntryUidCacheV1();
        ApplicationUidService service = mock(ApplicationUidService.class);

        cacheV1.put(ServiceUid.DEFAULT, "applicationName", ApplicationUid.of(200));
        UidFetcher uidFetcher = new UidFetcherV1(service, cacheV1);
        GrpcServerHeaderV1 serverHeader = new GrpcServerHeaderV1(header, uidFetcher);

        Supplier<ApplicationUid> applicationUid = serverHeader.getApplicationUid();
        assertEquals(ApplicationUid.of(200), applicationUid.get());
    }
}