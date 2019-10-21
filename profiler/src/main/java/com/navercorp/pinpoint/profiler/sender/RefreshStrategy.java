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

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.client.SocketAddressProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class RefreshStrategy implements UdpSocketAddressProvider {
    // JDK default DNS Cache time : 30
    public static final long DEFAULT_PORT_UNREACHABLE_REFRESH_DELAY = TimeUnit.SECONDS.toMillis(30);
    public static final long NORMAL_REFRESH_DELAY = TimeUnit.MINUTES.toMillis(5);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SocketAddressProvider socketAddressProvider;
    private final long normalRefreshDelay;
    private final long portUnreachableRefreshDelay;

    private InetSocketAddress socketAddress;
    private long lastRefreshTime;
    private boolean portUnreachableState = false;


    public RefreshStrategy(SocketAddressProvider socketAddressProvider) {
        this(socketAddressProvider, NORMAL_REFRESH_DELAY, DEFAULT_PORT_UNREACHABLE_REFRESH_DELAY);
    }

    public RefreshStrategy(SocketAddressProvider socketAddressProvider, long normalRefreshDelay, long portUnreachableRefreshDelay) {
        this.socketAddressProvider = Assert.requireNonNull(socketAddressProvider, "socketAddressProvider");
        this.normalRefreshDelay = normalRefreshDelay;
        this.portUnreachableRefreshDelay = portUnreachableRefreshDelay;
    }

    @Override
    public void handlePortUnreachable() {
        this.portUnreachableState = true;
    }

    @Override
    public InetSocketAddress resolve() {
        final boolean refresh = needRefresh();
        if (refresh) {
            final InetSocketAddress newAddress = socketAddressProvider.resolve();
            if (isResolved(newAddress)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("DNS refresh {}", newAddress);
                }
                this.socketAddress = newAddress;
            }
        }

        return this.socketAddress;
    }

    public boolean isResolved(InetSocketAddress newAddress) {
        if (newAddress == null) {
            return false;
        }
        return !newAddress.isUnresolved();
    }

    private boolean needRefresh() {
        final boolean portUnreachableStatus = resetPortUnreachableState();
        final long deadline = getDeadline(portUnreachableStatus);
        final long currentTimeMillis = tick();
        if (currentTimeMillis > deadline) {
            // reset deadLine
            this.lastRefreshTime = currentTimeMillis;
            return true;
        }
        return false;

    }

    @VisibleForTesting
    long tick() {
        return System.currentTimeMillis();
    }

    private long getDeadline(boolean portUnreachableStatus) {
        if (portUnreachableStatus) {
            return lastRefreshTime + portUnreachableRefreshDelay;
        } else {
            return lastRefreshTime + normalRefreshDelay;
        }
    }


    private boolean resetPortUnreachableState() {
        final boolean currentState = this.portUnreachableState;
        this.portUnreachableState = false;
        return currentState;
    }


}
