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
import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class TBaseFilterChain<T extends SocketAddress> implements TBaseFilter<T> {

    private final List<TBaseFilter<T>> filterChain;

    public TBaseFilterChain(List<TBaseFilter<T>> tBaseFilter) {
        if (tBaseFilter == null) {
            throw new NullPointerException("tBaseFilter must not be null");
        }
        this.filterChain = new ArrayList<>(tBaseFilter);
    }

    @Override
    public boolean filter(DatagramSocket localSocket, TBase<?, ?> tBase, T remoteHostAddress) {
        for (TBaseFilter tBaseFilter : filterChain) {
            if (tBaseFilter.filter(localSocket, tBase, remoteHostAddress) == TBaseFilter.BREAK) {
                return BREAK;
            }
        }
        return TBaseFilter.CONTINUE;
    }

}
