/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.thrift;

import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.rpc.server.ChannelFilter;
import org.jboss.netty.channel.Channel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AddressFilterAdaptor implements ChannelFilter {
    private final AddressFilter filter;

    public AddressFilterAdaptor(AddressFilter filter) {
        this.filter = Objects.requireNonNull(filter, "filter");
    }

    @Override
    public boolean accept(Channel channel) {
        final InetSocketAddress remoteAddress = (InetSocketAddress) channel.getRemoteAddress();
        if (remoteAddress == null) {
            return true;
        }
        InetAddress address = remoteAddress.getAddress();
        return filter.accept(address);
    }
}
