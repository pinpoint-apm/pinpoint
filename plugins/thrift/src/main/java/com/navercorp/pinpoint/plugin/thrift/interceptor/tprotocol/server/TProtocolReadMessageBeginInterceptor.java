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

package com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server;

import static com.navercorp.pinpoint.plugin.thrift.ThriftScope.THRIFT_SERVER_SCOPE;

import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.thrift.ThriftClientCallContext;
import com.navercorp.pinpoint.plugin.thrift.ThriftConstants;

/**
 * This interceptor retrieves the method name called by the client and stores it for other interceptors in the chain to use.
 * <p>
 * <tt>TBaseAsyncProcessorProcessInterceptor</tt> -> <b><tt>TProtocolReadMessageBeginInterceptor</tt></b> -> 
 * <tt>TProtocolReadFieldBeginInterceptor</tt> <-> <tt>TProtocolReadTTypeInterceptor</tt> -> <tt>TProtocolReadMessageEndInterceptor</tt>
 * <p>
 * Based on Thrift 0.8.0+
 * 
 * @author HyunGil Jeong
 * 
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageBeginInterceptor TProtocolReadMessageBeginInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadFieldBeginInterceptor TProtocolReadFieldBeginInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadTTypeInterceptor TProtocolReadTTypeInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageEndInterceptor TProtocolReadMessageEndInterceptor
 */
@Group(value=THRIFT_SERVER_SCOPE, executionPolicy=ExecutionPolicy.INTERNAL)
public class TProtocolReadMessageBeginInterceptor implements SimpleAroundInterceptor, ThriftConstants {
    
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final InterceptorGroup group;
    private final MetadataAccessor asyncMarker;

    public TProtocolReadMessageBeginInterceptor(
            @Name(THRIFT_SERVER_SCOPE) InterceptorGroup group,
            @Name(METADATA_ASYNC_MARKER) MetadataAccessor asyncMarker) {
        this.group = group;
        this.asyncMarker = asyncMarker;
    }
    
    @Override
    public void before(Object target, Object[] args) {
        // Do nothing
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (!shouldTrace(target)) {
            return;
        }
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        String methodName = UNKNOWN_METHOD_NAME;
        if (result instanceof TMessage) {
            TMessage message = (TMessage)result;
            methodName = message.name;
        }
        ThriftClientCallContext clientCallContext = new ThriftClientCallContext(methodName);
        InterceptorGroupInvocation currentTransaction = this.group.getCurrentInvocation();
        currentTransaction.setAttachment(clientCallContext);
    }
    
    private boolean shouldTrace(Object target) {
        if (target instanceof TProtocol) {
            TProtocol protocol = (TProtocol)target;
            if (this.asyncMarker.isApplicable(protocol) && this.asyncMarker.get(protocol) != null) {
                Boolean tracingAsyncServer = this.asyncMarker.get(protocol);
                return tracingAsyncServer;
            }
        }
        return false;
    }

}
