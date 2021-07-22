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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.SocketOption;
import java.net.StandardSocketOptions;

/**
 * @author Taejin Koo
 */
public class ReusePortSocketOptionApplier {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReusePortSocketOptionApplier.class);

    private static final String FIELD_NAME_SO_REUSEPORT = "SO_REUSEPORT";

    private static final SocketOption REUSE_PORT_SOCKET_OPTION = getReusePortSocketOption();

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
            Method setOptionMethod = DatagramSocket.class.getDeclaredMethod("setOption", SocketOption.class, Object.class);
            setOptionMethod.invoke(socket, REUSE_PORT_SOCKET_OPTION, true);
        } catch (Exception e) {
            LOGGER.warn("setOption invoke error", e);
            if (e instanceof IOException) {
                throw (IOException)e;
            }
            throw new IOException("setOption invoke error", e);
        }
    }

    public boolean isReusePortEnable() {
        return reusePortEnable;
    }

    public int getSocketCount() {
        return socketCount;
    }

    public static ReusePortSocketOptionApplier create(boolean reusePort, int socketCount) {
        if (REUSE_PORT_SOCKET_OPTION != null) {
            return new ReusePortSocketOptionApplier(reusePort, socketCount);
        }
        if (reusePort) {
            LOGGER.warn("ReusePort not supported, Please use Jvm9+ for using ReusePort SocketOption");
        }
        return new ReusePortSocketOptionApplier(false, socketCount);
    }

    private static SocketOption getReusePortSocketOption() {
        try {
            Field[] declaredFields = StandardSocketOptions.class.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                if (declaredField.getName().equals(FIELD_NAME_SO_REUSEPORT)) {
                    Object socketOption = declaredField.get(null);
                    if (socketOption instanceof SocketOption) {
                        LOGGER.info("{} option found", FIELD_NAME_SO_REUSEPORT);
                        return (SocketOption) socketOption;
                    }
                }
            }
        } catch (Exception ignore) {
            // ignores
        }
        return null;
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
