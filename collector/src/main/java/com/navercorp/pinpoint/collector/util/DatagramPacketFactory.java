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

package com.navercorp.pinpoint.collector.util;

import java.net.DatagramPacket;

/**
 * @author emeroad
 */
public class DatagramPacketFactory implements ObjectPoolFactory<DatagramPacket> {

    public static final int UDP_MAX_PACKET_LENGTH = 65507;

    private final int bufferLength;

    public DatagramPacketFactory() {
        this(UDP_MAX_PACKET_LENGTH);
    }

    public DatagramPacketFactory(int bufferLength) {
        if (bufferLength < 0 ) {
            throw new IllegalArgumentException("negative bufferLength:" + bufferLength);
        }
        this.bufferLength = bufferLength;
    }

    @Override
    public DatagramPacket create() {
        byte[] bytes = new byte[bufferLength];
        return new DatagramPacket(bytes, 0, bytes.length);
    }

    @Override
    public void beforeReturn(DatagramPacket packet) {
        packet.setLength(bufferLength);
    }
}
