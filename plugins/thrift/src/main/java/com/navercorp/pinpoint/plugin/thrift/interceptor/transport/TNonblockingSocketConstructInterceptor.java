/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift.interceptor.transport;

import java.net.Socket;

import org.apache.thrift.transport.TNonblockingSocket;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.thrift.ThriftConstants;

/**
 * @author HyunGil Jeong
 */
public class TNonblockingSocketConstructInterceptor implements SimpleAroundInterceptor, ThriftConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    
    private final MetadataAccessor socketAccessor;
    private final MetadataAccessor nonblockingSocketAddressAccessor;
    
    public TNonblockingSocketConstructInterceptor(
            @Name(METADATA_SOCKET) MetadataAccessor socketAccessor,
            @Name(METADATA_NONBLOCKING_SOCKET_ADDRESS) MetadataAccessor nonblockingSocketAddressAccessor) {
        this.socketAccessor = socketAccessor;
        this.nonblockingSocketAddressAccessor = nonblockingSocketAddressAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        // Do nothing
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        if (validate(target, args)) {
            Socket socket = ((TNonblockingSocket)target).getSocketChannel().socket();
            this.socketAccessor.set(target, socket);
            Object socketAddress = args[2];
            this.nonblockingSocketAddressAccessor.set(target, socketAddress);
        }
    }

    private boolean validate(Object target, Object[] args) {
        if (!(target instanceof TNonblockingSocket)) {
            return false;
        }
        if (args.length != 3) {
            return false;
        }
        if (!this.socketAccessor.isApplicable(target)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need metadata accessor ({})", METADATA_SOCKET);
            }
            return false;
        }
        if (!this.nonblockingSocketAddressAccessor.isApplicable(target)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need metadata accessor ({})", METADATA_NONBLOCKING_SOCKET_ADDRESS);
            }
            return false;
        }
        return true;
    }

}
