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

import io.grpc.Attributes;
import io.grpc.ConnectivityState;
import io.grpc.ConnectivityStateInfo;
import io.grpc.EquivalentAddressGroup;
import io.grpc.LoadBalancer;
import io.grpc.ManagedChannel;
import io.grpc.SynchronizationContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author youngjin.kim2
 */
public class MockLoadBalancerHelper extends LoadBalancer.Helper {
    private final String authority;
    final List<ConnectivityState> states = new ArrayList<>();
    final List<LoadBalancer.SubchannelPicker> pickers = new ArrayList<>();
    final List<MockSubchannel> subchannels = new ArrayList<>();
    final SynchronizationContext syncContext = new SynchronizationContext((a, b) -> {});

    public MockLoadBalancerHelper(String authority) {
        this.authority = authority;
    }

    public LoadBalancer.SubchannelPicker getLatestPicker() {
        if (pickers.isEmpty()) {
            return null;
        }
        return pickers.get(pickers.size() - 1);
    }

    public MockSubchannel getLatestSubchannel() {
        if (subchannels.isEmpty()) {
            return null;
        }
        return subchannels.get(subchannels.size() - 1);
    }

    @Override
    public ManagedChannel createOobChannel(EquivalentAddressGroup eag, String authority) {
        return null;
    }

    @Override
    public void updateBalancingState(@Nonnull ConnectivityState newState, @Nonnull LoadBalancer.SubchannelPicker newPicker) {
        states.add(newState);
        pickers.add(newPicker);
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public LoadBalancer.Subchannel createSubchannel(LoadBalancer.CreateSubchannelArgs args) {
        final MockSubchannel sc = new MockSubchannel(args);
        subchannels.add(sc);
        return sc;
    }

    @Override
    public SynchronizationContext getSynchronizationContext() {
        return syncContext;
    }

    @Override
    public void refreshNameResolution() {

    }

    final class MockSubchannel extends LoadBalancer.Subchannel {
        final Attributes attributes;
        boolean ready = false;
        boolean terminated = false;
        LoadBalancer.SubchannelStateListener listener;
        List<EquivalentAddressGroup> addresses;

        public MockSubchannel(LoadBalancer.CreateSubchannelArgs args) {
            this.attributes = args.getAttributes();
            this.addresses = args.getAddresses();
        }

        @Override
        public void shutdown() {
            terminated = true;
            if (listener != null) {
                syncContext.execute(() -> listener.onSubchannelState(ConnectivityStateInfo.forNonError(ConnectivityState.SHUTDOWN)));
            }
        }

        @Override
        public void requestConnection() {
            if (listener != null) {
                syncContext.execute(() -> listener.onSubchannelState(ConnectivityStateInfo.forNonError(ConnectivityState.CONNECTING)));
            }
            ready = true;
            if (listener != null) {
                syncContext.execute(() -> listener.onSubchannelState(ConnectivityStateInfo.forNonError(ConnectivityState.READY)));
            }
        }

        @Override
        public Attributes getAttributes() {
            return attributes;
        }

        @Override
        public void start(LoadBalancer.SubchannelStateListener listener) {
            this.listener = listener;
            if (listener != null) {
                syncContext.execute(() -> listener.onSubchannelState(ConnectivityStateInfo.forNonError(ConnectivityState.IDLE)));
            }
        }

        @Override
        public List<EquivalentAddressGroup> getAllAddresses() {
            return addresses;
        }

        @Override
        public void updateAddresses(List<EquivalentAddressGroup> addrs) {
            this.addresses = addrs;
        }
    }
}
