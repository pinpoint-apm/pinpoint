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

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class TBaseFilterChain implements TBaseFilter {

    private final List<TBaseFilter> filterChain = new ArrayList<TBaseFilter>();

    public void addTBaseFilter(TBaseFilter tBaseFilter) {
        if (tBaseFilter == null) {
            throw new NullPointerException("tBaseFilter must not be null");
        }
        this.filterChain.add(tBaseFilter);
    }

    @Override
    public boolean filter(TBase<?, ?> tBase, DatagramPacket packet) {
        for (TBaseFilter tBaseFilter : filterChain) {
            if (tBaseFilter.filter(tBase, packet) == TBaseFilter.BREAK) {
                return BREAK;
            }
        }
        return TBaseFilter.CONTINUE;
    }
}
