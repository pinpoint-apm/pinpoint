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

package com.navercorp.pinpoint.collector.receiver.thrift.udp;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.Collections;

import static org.mockito.Mockito.mock;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TBaseFilterChainTest {

    @Test
    public void constructor_generic_array() {
        TBaseFilter<InetSocketAddress> filter = mock(TBaseFilter.class);
        TBaseFilterChain<InetSocketAddress> chain = new TBaseFilterChain<>(Collections.singletonList(filter));
        chain.filter(null, null, null);
    }
}