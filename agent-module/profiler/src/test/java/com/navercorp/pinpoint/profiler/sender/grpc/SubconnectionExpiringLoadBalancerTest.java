/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.sender.grpc;

import io.grpc.ConnectivityState;
import io.grpc.ConnectivityStateInfo;
import io.grpc.EquivalentAddressGroup;
import io.grpc.LoadBalancer;
import io.grpc.Status;
import io.grpc.SynchronizationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class SubconnectionExpiringLoadBalancerTest {
    @Test
    public void shouldExpirePeriodically() {
        final String authority = "127.0.0.1";
        final long renewPeriodMillis = -1;
        final MockLoadBalancerHelper helper = new MockLoadBalancerHelper(authority);
        final SynchronizationContext syncContext = helper.getSynchronizationContext();
        final SubconnectionExpiringLoadBalancer lb = new SubconnectionExpiringLoadBalancer(helper, renewPeriodMillis, TimeUnit.MILLISECONDS);

        syncContext.execute(() -> lb.handleResolvedAddresses(mockAddresses()));

        final LoadBalancer.PickResult firstPicked = helper.getLatestPicker().pickSubchannel(null);
        final LoadBalancer.PickResult secondPicked = helper.getLatestPicker().pickSubchannel(null);
        final LoadBalancer.PickResult thirdPicked = helper.getLatestPicker().pickSubchannel(null);

        assertThat(firstPicked).isNotEqualTo(secondPicked);
        assertThat(secondPicked).isNotEqualTo(thirdPicked);
    }

    @Test
    public void shouldUpdateAddress() {
        final String authority = "127.0.0.1";
        final long renewPeriodMillis = -1;
        final MockLoadBalancerHelper helper = new MockLoadBalancerHelper(authority);
        final SynchronizationContext syncContext = helper.getSynchronizationContext();
        final SubconnectionExpiringLoadBalancer lb = new SubconnectionExpiringLoadBalancer(helper, renewPeriodMillis, TimeUnit.MILLISECONDS);

        final LoadBalancer.ResolvedAddresses addr1 = mockAddresses();
        final LoadBalancer.ResolvedAddresses addr2 = mockAddresses();

        syncContext.execute(() -> lb.handleResolvedAddresses(addr1));
        final MockLoadBalancerHelper.MockSubchannel sc = helper.getLatestSubchannel();
        assertThat(sc.getAllAddresses()).isEqualTo(addr1.getAddresses());

        syncContext.execute(() -> lb.handleResolvedAddresses(addr2));
        assertThat(sc.getAllAddresses()).isEqualTo(addr2.getAddresses());

        syncContext.execute(() -> sc.listener.onSubchannelState(ConnectivityStateInfo.forTransientFailure(Status.UNKNOWN)));
        syncContext.execute(() -> lb.handleResolvedAddresses(addr1));
        assertThat(sc.getAllAddresses()).isEqualTo(addr1.getAddresses());

        syncContext.execute(() -> sc.listener.onSubchannelState(ConnectivityStateInfo.forNonError(ConnectivityState.CONNECTING)));
        syncContext.execute(() -> lb.handleResolvedAddresses(addr2));
        assertThat(sc.getAllAddresses()).isEqualTo(addr2.getAddresses());
    }

    @Test
    public void shouldTerminateProperly() {
        final String authority = "127.0.0.1";
        final long renewPeriodMillis = -1;
        final MockLoadBalancerHelper helper = new MockLoadBalancerHelper(authority);
        final SynchronizationContext syncContext = helper.getSynchronizationContext();
        final SubconnectionExpiringLoadBalancer lb = new SubconnectionExpiringLoadBalancer(helper, renewPeriodMillis, TimeUnit.MILLISECONDS);

        final LoadBalancer.ResolvedAddresses addr = mockAddresses();

        syncContext.execute(() -> lb.handleResolvedAddresses(addr));
        syncContext.execute(() -> lb.shutdown());

        assertThat(helper.subchannels).isNotEmpty();
        for (MockLoadBalancerHelper.MockSubchannel sc: helper.subchannels) {
            assertThat(sc.terminated).isTrue();
        }
    }

    private LoadBalancer.ResolvedAddresses mockAddresses() {
        final SocketAddress addr = new SocketAddress() {};
        final List<SocketAddress> addrs = Collections.singletonList(addr);

        final EquivalentAddressGroup addrGroup = new EquivalentAddressGroup(addrs);
        final List<EquivalentAddressGroup> addrGroups = Collections.singletonList(addrGroup);

        return LoadBalancer.ResolvedAddresses
                .newBuilder()
                .setAddresses(addrGroups)
                .build();
    }

}
