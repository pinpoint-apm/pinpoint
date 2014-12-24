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

import org.apache.hadoop.hbase.util.Bytes;

import java.net.DatagramPacket;

/**
 * @author emeroad
 */
public final class PacketUtils {
    private PacketUtils() {
    }

    public static String dumpDatagramPacket(DatagramPacket datagramPacket) {
        if (datagramPacket == null) {
            return "null";
        }
        return Bytes.toStringBinary(datagramPacket.getData(), 0, datagramPacket.getLength());
    }

    public static String dumpByteArray(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        return Bytes.toStringBinary(bytes, 0, bytes.length);
    }
}
