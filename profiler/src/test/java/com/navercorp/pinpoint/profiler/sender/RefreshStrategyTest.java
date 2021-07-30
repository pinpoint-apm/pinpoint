/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sender;

import com.navercorp.pinpoint.rpc.client.SocketAddressProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.InetSocketAddress;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
@RunWith(MockitoJUnitRunner.class)
public class RefreshStrategyTest {

    @Mock
    private SocketAddressProvider socketAddressProvider;

    @Before
    public void setUp() throws Exception {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8080);
        when(socketAddressProvider.resolve()).thenReturn(address);
    }

    @Test
    public void resolve_refresh() {

        UdpSocketAddressProvider refreshStrategy = new RefreshStrategy(socketAddressProvider, -1, 0);
        // first lookup
        refreshStrategy.resolve();
        // refresh lookup
        refreshStrategy.resolve();

        Mockito.verify(socketAddressProvider, times(2)).resolve();
    }

    @Test
    public void resolve_norefresh() {

        UdpSocketAddressProvider refreshStrategy = new RefreshStrategy(socketAddressProvider, 10*1000, 0);
        // first lookup
        refreshStrategy.resolve();
        // refresh lookup
        refreshStrategy.resolve();

        Mockito.verify(socketAddressProvider, times(1)).resolve();
    }

    @Test
    public void handlePortUnreachable_refresh() {
        UdpSocketAddressProvider refreshStrategy = new RefreshStrategy(socketAddressProvider, 10*1000, -1);
        // first lookup
        refreshStrategy.resolve();
        refreshStrategy.handlePortUnreachable();
        // refresh lookup
        refreshStrategy.resolve();

        Mockito.verify(socketAddressProvider, times(2)).resolve();
    }

    @Test
    public void handlePortUnreachable_norefresh() {
        UdpSocketAddressProvider refreshStrategy = new RefreshStrategy(socketAddressProvider, 10*1000, 10*1000);
        // first lookup
        refreshStrategy.resolve();
        refreshStrategy.handlePortUnreachable();
        // refresh lookup
        refreshStrategy.resolve();

        Mockito.verify(socketAddressProvider, times(1)).resolve();
    }
}