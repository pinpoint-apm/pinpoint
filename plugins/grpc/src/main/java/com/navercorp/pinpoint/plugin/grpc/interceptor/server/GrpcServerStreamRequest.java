/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.grpc.interceptor.server;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.plugin.grpc.GrpcConstants;
import io.grpc.Attributes;
import io.grpc.Metadata;
import io.grpc.internal.ServerStream;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class GrpcServerStreamRequest {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ServerStream serverStream;
    private final String methodName;
    private final Metadata metadata;

    static GrpcServerStreamRequest create(Object[] args) {
        if (validate(args)) {
            return new GrpcServerStreamRequest((ServerStream) args[0], (String) args[1], (Metadata) args[2]);
        }
        return null;
    }

    static boolean validate(Object[] args) {
        if (ArrayUtils.getLength(args) == 3) {
            if (!(args[0] instanceof io.grpc.internal.ServerStream)) {
                return false;
            }
            if (!(args[1] instanceof String)) {
                return false;
            }
            if (!(args[2] instanceof io.grpc.Metadata)) {
                return false;
            }
            return true;
        }
        return false;
    }

    GrpcServerStreamRequest(ServerStream serverStream, String methodName, Metadata metadata) {
        this.serverStream = Assert.requireNonNull(serverStream, "serverStream");
        this.methodName = Assert.requireNonNull(methodName, "methodName");
        this.metadata = Assert.requireNonNull(metadata, "metadata");
    }

    public ServerStream getServerStream() {
        return serverStream;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getHeader(String name) {
        final Metadata.Key<String> key = Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);
        Iterable<String> headerValues = metadata.removeAll(key);

        String headerValue = null;
        if (headerValues != null) {
            Iterator<String> iterator = headerValues.iterator();
            if (iterator.hasNext()) {
                headerValue = iterator.next();
                if (iterator.hasNext()) {
                    headerValue = null;
                }
            }
        }

        return headerValue;
    }

    String getRemoteAddress() {
        Attributes attributes = serverStream.getAttributes();
        if (attributes == null) {
            return null;
        }

        try {
            // keys method is being considered for removal,
            Set<Attributes.Key<?>> keys = attributes.keys();
            if (keys == null) {
                if (isDebug) {
                    logger.debug("can't attributes keys");
                }
                return null;
            }

            for (Attributes.Key<?> key : keys) {
                if (key != null && key.toString().equals("remote-addr")) {
                    Object remoteAddress = attributes.get(key);
                    if (remoteAddress instanceof SocketAddress) {
                        return getSocketAddressAsString((SocketAddress) remoteAddress);
                    } else if (remoteAddress instanceof String) {
                        return (String) remoteAddress;
                    }
                }
            }
        } catch (Exception e) {
            if (isDebug) {
                logger.debug("can't find keys method");
            }
        }

        return GrpcConstants.UNKNOWN_ADDRESS;
    }


    public static String getSocketAddressAsString(SocketAddress socketAddress) {
        if (socketAddress instanceof InetSocketAddress) {
            final InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            final InetAddress remoteAddress = inetSocketAddress.getAddress();
            if (remoteAddress != null) {
                return HostAndPort.toHostAndPortString(remoteAddress.getHostAddress(), inetSocketAddress.getPort());
            }
        }

        return GrpcConstants.UNKNOWN_ADDRESS;
    }


    String getServerAddress() {
        return serverStream.getAuthority();
    }

}
