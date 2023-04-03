/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver;

import com.google.common.net.InetAddresses;
import com.navercorp.pinpoint.collector.receiver.thrift.AddressFilterAdaptor;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.common.server.util.IgnoreAddressFilter;
import org.jboss.netty.channel.Channel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AddressFilterAdaptorTest {

    private final String ignoreString = "10.0.0.1";
    private final InetAddress ignore = InetAddresses.forString(ignoreString);


    @Test
    public void accept_accept() {
        AddressFilter filter = mock(AddressFilter.class);
        when(filter.accept(any())).thenReturn(true);

        Channel ignoreChannel = mockChannel(ignore);
        AddressFilterAdaptor adaptor = new AddressFilterAdaptor(filter);

        Assertions.assertTrue(adaptor.accept(ignoreChannel));
    }

    @Test
    public void accept_reject() {
        String ignoreString = "10.0.0.1";
        AddressFilter ignoreAddressFilter = new IgnoreAddressFilter(List.of(ignoreString));

        Channel ignoreChannel = mockChannel(ignore);
        AddressFilterAdaptor adaptor = new AddressFilterAdaptor(ignoreAddressFilter);

        Assertions.assertFalse(adaptor.accept(ignoreChannel));
    }

    private Channel mockChannel(InetAddress inetAddress) {
        Channel channel = mock(Channel.class);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, 80);
        when(channel.getRemoteAddress()).thenReturn(inetSocketAddress);
        return channel;
    }
}