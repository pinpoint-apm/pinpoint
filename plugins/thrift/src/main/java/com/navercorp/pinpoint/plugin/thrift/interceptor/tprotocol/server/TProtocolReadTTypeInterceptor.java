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

import static com.navercorp.pinpoint.plugin.thrift.ThriftClientCallContext.NONE;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.thrift.ThriftClientCallContext;
import com.navercorp.pinpoint.plugin.thrift.ThriftRequestProperty;
import com.navercorp.pinpoint.plugin.thrift.ThriftHeader;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.ServerMarkerFlagFieldAccessor;

/**
 * This interceptor reads a data field and if applicable, populates the corresponding parent trace data as marked by the previous interceptor.
 * <ul>
 * <li>Synchronous
 * <p>
 * <tt>TBaseProcessorProcessInterceptor</tt> -> <tt>ProcessFunctionProcessInterceptor</tt> -> <tt>TProtocolReadFieldBeginInterceptor</tt> <-> <b>
 * <tt>TProtocolReadTTypeInterceptor</tt></b> -> <tt>TProtocolReadMessageEndInterceptor</tt></li>
 * <li>Asynchronous
 * <p>
 * <tt>TBaseAsyncProcessorProcessInterceptor</tt> -> <tt>TProtocolReadMessageBeginInterceptor</tt> -> <tt>TProtocolReadFieldBeginInterceptor</tt> <-> <b>
 * <tt>TProtocolReadTTypeInterceptor</tt></b> -> <tt>TProtocolReadMessageEndInterceptor</tt>
 * </ul>
 * <p>
 * Based on Thrift 0.8.0+
 * 
 * @author HyunGil Jeong
 * 
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.server.TBaseProcessorProcessInterceptor TBaseProcessorProcessInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.server.ProcessFunctionProcessInterceptor ProcessFunctionProcessInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.server.async.TBaseAsyncProcessorProcessInterceptor TBaseAsyncProcessProcessInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageBeginInterceptor TProtocolReadMessageBeginInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadFieldBeginInterceptor TProtocolReadFieldBeginInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageEndInterceptor TProtocolReadMessageEndInterceptor
 */
public class TProtocolReadTTypeInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final InterceptorScope scope;

    public TProtocolReadTTypeInterceptor(InterceptorScope scope) {
        this.scope = scope;
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
        if (!validate(target)) {
            return;
        }
        final boolean shouldTrace = ((ServerMarkerFlagFieldAccessor)target)._$PINPOINT$_getServerMarkerFlag();
        if (shouldTrace) {
            InterceptorScopeInvocation currentTransaction = this.scope.getCurrentInvocation();
            Object attachment = currentTransaction.getAttachment();
            if (attachment instanceof ThriftClientCallContext) {
                ThriftClientCallContext clientCallContext = (ThriftClientCallContext)attachment;
                ThriftHeader headerKeyToBeRead = clientCallContext.getTraceHeaderToBeRead();
                if (headerKeyToBeRead == NONE) {
                    return;
                }
                ThriftRequestProperty parentTraceInfo = clientCallContext.getTraceHeader();
                if (parentTraceInfo == null) {
                    parentTraceInfo = new ThriftRequestProperty();
                    clientCallContext.setTraceHeader(parentTraceInfo);
                }
                try {
                    parentTraceInfo.setTraceHeader(headerKeyToBeRead, result);
                } catch (Throwable t) {
                    logger.warn("Error reading trace header.", t);
                } finally {
                    clientCallContext.setTraceHeaderToBeRead(NONE);
                }
            }
        }
    }

    private boolean validate(Object target) {
        if (!(target instanceof ServerMarkerFlagFieldAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", ServerMarkerFlagFieldAccessor.class.getName());
            }
            return false;
        }
        return true;
    }

}
