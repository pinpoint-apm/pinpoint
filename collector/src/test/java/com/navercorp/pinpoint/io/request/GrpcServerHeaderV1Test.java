package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.collector.receiver.grpc.cache.SingleEntryUidCacheV1;
import com.navercorp.pinpoint.collector.receiver.grpc.cache.UidCache;
import com.navercorp.pinpoint.common.server.vo.ApplicationUid;
import com.navercorp.pinpoint.common.server.vo.ServiceUid;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderV1;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GrpcServerHeaderV1Test {

    @Test
    void getApplicationUid_cache_miss() {

        Header header = new HeaderV1("headername", "agentId", "agentName", "applicationName", 1, 0L, 0, List.of(), false, Map.of());
        UidCache cacheV1 = new SingleEntryUidCacheV1();
        GrpcServerHeaderV1 serverHeader = new GrpcServerHeaderV1(header, cacheV1);

        ApplicationUid applicationUid = serverHeader.getApplicationUid();
        assertEquals(ApplicationUid.of(100), applicationUid);
    }


    @Test
    void getApplicationUid_cache_hit() {

        Header header = new HeaderV1("headername", "agentId", "agentName", "applicationName", 1, 0L, 0, List.of(), false, Map.of());
        UidCache cacheV1 = new SingleEntryUidCacheV1();
        cacheV1.put(ServiceUid.DEFAULT_SERVICE_UID, "applicationName", ApplicationUid.of(200));
        GrpcServerHeaderV1 serverHeader = new GrpcServerHeaderV1(header, cacheV1);

        ApplicationUid applicationUid = serverHeader.getApplicationUid();
        assertEquals(ApplicationUid.of(200), applicationUid);
    }
}