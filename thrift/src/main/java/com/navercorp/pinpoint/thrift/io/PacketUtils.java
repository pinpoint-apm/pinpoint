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

package com.navercorp.pinpoint.thrift.io;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author emeroad
 */
public class PacketUtils {

    public static byte[] sliceData(DatagramPacket packet, int startOffset) {
        Objects.requireNonNull(packet, "packet");

        int packetLength = packet.getLength();
        int packetOffset = packet.getOffset();
        byte[] source = packet.getData();
        return Arrays.copyOfRange(source, packetOffset + startOffset, packetLength);
    }

    public static byte[] sliceData(byte[] packet, int startOffset, int length) {
        Objects.requireNonNull(packet, "packet");
        return Arrays.copyOfRange(packet, startOffset, length);
    }
}
