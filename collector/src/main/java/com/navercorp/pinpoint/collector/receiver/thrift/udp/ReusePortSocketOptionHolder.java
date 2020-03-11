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

import java.lang.reflect.Field;
import java.net.SocketOption;
import java.net.StandardSocketOptions;

/**
 * @author Taejin Koo
 */
public class ReusePortSocketOptionHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReusePortSocketOptionHolder.class);

    private static final String FIELD_NAME_SO_REUSEPORT = "SO_REUSEPORT";

    private final SocketOption socketOption;
    private final boolean enable;
    private final int socketCount;

    private ReusePortSocketOptionHolder(SocketOption socketOption, boolean enable, int socketCount) {
        this.socketOption = socketOption;
        this.enable = enable;
        this.socketCount = socketCount;
    }

    public SocketOption getSocketOption() {
        return socketOption;
    }

    public boolean isEnable() {
        return enable;
    }

    public int getSocketCount() {
        return socketCount;
    }

    public static ReusePortSocketOptionHolder create(int socketCount) {
        SocketOption reusePortSocketOption = getReusePortSocketOption();
        if (reusePortSocketOption == null) {
            LOGGER.warn("Failed to get ReusePort SocketOption. Please use Jvm9+ for using ReusePort SocketOption");
            return null;
        }

        return new ReusePortSocketOptionHolder(reusePortSocketOption, true, socketCount);
    }

    private static SocketOption getReusePortSocketOption() {
        try {
            Field[] declaredFields = StandardSocketOptions.class.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                if (declaredField.getName().equals(FIELD_NAME_SO_REUSEPORT)) {
                    Object socketOption = declaredField.get(null);
                    if (socketOption instanceof SocketOption) {
                        return (SocketOption) socketOption;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to get ReusePort SocketOption. caused:{}", e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReusePortSocketOptionHolder{");
        sb.append("socketOption=").append(socketOption);
        sb.append(", enable=").append(enable);
        sb.append(", socketCount=").append(socketCount);
        sb.append('}');
        return sb.toString();
    }

}
