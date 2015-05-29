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

package com.navercorp.pinpoint.plugin.thrift.interceptor.client.async;

import com.navercorp.pinpoint.bootstrap.FieldAccessor;
import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.thrift.ThriftConstants;

/**
 * @author HyunGil Jeong
 */
public class TAsyncMethodCallConstructInterceptor implements SimpleAroundInterceptor, ThriftConstants {
   
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    
    private final MetadataAccessor nonblockingSocketAddressAccessor;
    private final FieldAccessor transportFieldAccessor;
    
    public TAsyncMethodCallConstructInterceptor(
            @Name(METADATA_NONBLOCKING_SOCKET_ADDRESS) MetadataAccessor nonblockingSocketAddressAccessor,
            @Name(FIELD_TRANSPORT_ASYNC_METHOD_CALL) FieldAccessor transportFieldAccessor) {
        this.nonblockingSocketAddressAccessor = nonblockingSocketAddressAccessor;
        this.transportFieldAccessor = transportFieldAccessor;
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
        if (validate(target)) {
            Object nonblockingTransportObj = this.transportFieldAccessor.get(target);
            if (validateTransport(nonblockingTransportObj)) {
                Object socketAddress = this.nonblockingSocketAddressAccessor.get(nonblockingTransportObj);
                this.nonblockingSocketAddressAccessor.set(target, socketAddress);
            }
        }
    }

    private boolean validate(Object target) {
        if (!this.transportFieldAccessor.isApplicable(target)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({})", FIELD_TRANSPORT_ASYNC_METHOD_CALL);
            }
            return false;
        }
        if (!this.nonblockingSocketAddressAccessor.isApplicable(target)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({})", METADATA_NONBLOCKING_SOCKET_ADDRESS);
            }
            return false;
        }
        return true;
    }
    
    private boolean validateTransport(Object nonblockingTransportObj) {
        if (nonblockingTransportObj == null) {
            if (isDebug) {
                logger.debug("Target field object is null.", FIELD_TRANSPORT_ASYNC_METHOD_CALL);
            }
            return false;
        }
        if (!this.nonblockingSocketAddressAccessor.isApplicable(nonblockingTransportObj)) {
            if (isDebug) {
                logger.debug("Invalid transport object. Need metadata accessor({})", METADATA_NONBLOCKING_SOCKET_ADDRESS);
            }
            return false;
        }
        return true;
    }

}
