/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.thrift.udp;

import com.navercorp.pinpoint.common.util.OsType;
import com.navercorp.pinpoint.common.util.OsUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.StandardSocketOptions;

/**
 * @author Taejin Koo
 */
public class ReusePortSocketOptionApplier {

    private static final Logger LOGGER = LogManager.getLogger(ReusePortSocketOptionApplier.class);

    private static final OsType[] UNSUPPORTED_OS = new OsType[]{OsType.WINDOW, OsType.SOLARIS};

    private final boolean reusePortEnable;
    private final int socketCount;

    private ReusePortSocketOptionApplier(boolean reusePortEnable, int socketCount) {
        this.reusePortEnable = reusePortEnable;
        this.socketCount = socketCount;
    }

    public void apply(DatagramSocket socket) throws IOException {
        if (!reusePortEnable) {
            return;
        }
        try {
            socket.setOption(StandardSocketOptions.SO_REUSEPORT, true);
        } catch (IOException e) {
            LOGGER.warn("setOption invoke error", e);
        }
    }

    public boolean isReusePortEnable() {
        return reusePortEnable;
    }

    public int getSocketCount() {
        return socketCount;
    }

    public static ReusePortSocketOptionApplier create(boolean reusePort, int socketCount) {
        if (isUnsupportedOS()) {
            if (reusePort) {
                LOGGER.warn("ReusePort not supported, OS:{}", OsUtils.getType());
            }
            return new ReusePortSocketOptionApplier(false, socketCount);
        }

        return new ReusePortSocketOptionApplier(reusePort, socketCount);
    }

    private static boolean isUnsupportedOS() {
        final OsType osType = OsUtils.getType();
        for (OsType unsupportedO : UNSUPPORTED_OS) {
            if (osType.equals(unsupportedO)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReusePortSocketOptionApplier{");
        sb.append("reusePortEnable=").append(reusePortEnable);
        sb.append(", socketCount=").append(socketCount);
        sb.append('}');
        return sb.toString();
    }

}
