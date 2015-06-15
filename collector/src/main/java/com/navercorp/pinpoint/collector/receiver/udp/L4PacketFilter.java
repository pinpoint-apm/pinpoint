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

import com.navercorp.pinpoint.thrift.io.L4Packet;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class L4PacketFilter<T> implements TBaseFilter<T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean filter(TBase<?, ?> tBase, T remoteAddress) {
        if (tBase instanceof L4Packet) {
            if (logger.isDebugEnabled()) {
                L4Packet l4Packet = (L4Packet) tBase;
                logger.debug("udp l4 packet {} {}", l4Packet.getHeader(), remoteAddress);
            }
            return BREAK;
        }
        return CONTINUE;
    }
}
