/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.udp;

import org.apache.thrift.TBase;

import java.net.DatagramSocket;
import java.net.SocketAddress;

/**
 * @author emeroad
 */
public interface TBaseFilter<T> {
    boolean CONTINUE = true;
    boolean BREAK = false;

    boolean filter(DatagramSocket localSocket, TBase<?, ?> tBase, T remoteHostAddress);

    // TODO fix generic type
    TBaseFilter CONTINUE_FILTER = new TBaseFilter<SocketAddress>() {

        @Override
        public boolean filter(DatagramSocket localSocket, TBase<?, ?> tBase, SocketAddress remoteHostAddress) {
            return CONTINUE;
        }

    };
}
