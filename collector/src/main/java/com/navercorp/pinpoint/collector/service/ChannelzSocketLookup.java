/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.service;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * @author youngjin.kim2
 */
public interface ChannelzSocketLookup {

    Collection<SocketEntry> find(
            @Nullable String remoteAddress,
            @Nullable Integer localPort
    );

    class SocketEntry {
        private final String remoteAddr;
        private final Integer localPort;
        private final long socketId;

        private SocketEntry(String remoteAddr, Integer localPort, long socketId) {
            this.remoteAddr = remoteAddr;
            this.localPort = localPort;
            this.socketId = socketId;
        }

        /**
         * `remote.toString()`, or `local.toString()` are expected to be like `/127.0.0.1:12345`
         * @param remote remote address
         * @param local local address
         * @param socketId socket id
         * @return entry of socket index
         */
        public static SocketEntry compose(Object remote, Object local, long socketId) {
            String remoteAddr = split(remote)[0];
            String localPort = split(local)[1];
            return new SocketEntry(remoteAddr.substring(1), parse(localPort), socketId);
        }

        private static String[] split(Object obj) {
            if (obj == null) {
                return null;
            }
            return obj.toString().split(":", 2);
        }

        private static Integer parse(String str) {
            if (str == null) {
                return null;
            }
            return Integer.parseInt(str);
        }

        public boolean match(
                @Nullable String cmpRemoteAddr,
                @Nullable Integer cmpLocalPort
        ) {
            if (cmpRemoteAddr != null && !cmpRemoteAddr.equals(remoteAddr)) {
                return false;
            }
            return cmpLocalPort == null || cmpLocalPort.equals(localPort);
        }

        public long getSocketId() {
            return this.socketId;
        }
    }

}
