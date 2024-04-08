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

package com.navercorp.pinpoint.plugin.thrift.interceptor.server.async;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.thrift.ThriftClientCallContext;
import com.navercorp.pinpoint.plugin.thrift.ThriftClientCallContextAttachmentFactory;
import com.navercorp.pinpoint.plugin.thrift.ThriftUtils;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.AsyncMarkerFlagFieldAccessor;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.ServerMarkerFlagFieldAccessor;
import org.apache.thrift.TBaseAsyncProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.AbstractNonblockingServer.AsyncFrameBuffer;

/**
 * Entry/exit point for tracing asynchronous processors for Thrift services.
 * <p>
 * Because trace objects cannot be created until the message is read, this interceptor works in tandem with other interceptors in the tracing pipeline. The
 * actual processing of input messages is not off-loaded to <tt>AsyncProcessFunction</tt> (unlike synchronous processors where <tt>ProcessFunction</tt> does
 * most of the work).
 * <ol>
 * <li>
 * <p>
 * {@link com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageBeginInterceptor TProtocolReadMessageBeginInterceptor} retrieves
 * the method name called by the client.</li>
 * </p>
 *
 * <li>
 * <p>
 * {@link com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadFieldBeginInterceptor TProtocolReadFieldBeginInterceptor},
 * {@link com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadTTypeInterceptor TProtocolReadTTypeInterceptor} reads the header fields
 * and injects the parent trace object (if any).</li></p>
 *
 * <li>
 * <p>
 * {@link com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageEndInterceptor TProtocolReadMessageEndInterceptor} creates the
 * actual root trace object.</li></p> </ol>
 * <p>
 * <b><tt>TBaseAsyncProcessorProcessInterceptor</tt></b> -> <tt>TProtocolReadMessageBeginInterceptor</tt> -> <tt>TProtocolReadFieldBeginInterceptor</tt> <->
 * <tt>TProtocolReadTTypeInterceptor</tt> -> <tt>TProtocolReadMessageEndInterceptor</tt>
 * <p>
 * Based on Thrift 0.9.1+
 *
 * @author HyunGil Jeong
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageBeginInterceptor TProtocolReadMessageBeginInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadFieldBeginInterceptor TProtocolReadFieldBeginInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadTTypeInterceptor TProtocolReadTTypeInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageEndInterceptor TProtocolReadMessageEndInterceptor
 */
public class TBaseAsyncProcessorProcessInterceptor implements AroundInterceptor {

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final InterceptorScope scope;

    public TBaseAsyncProcessorProcessInterceptor(InterceptorScope scope) {
        this.scope = scope;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        // process(final AsyncFrameBuffer fb)
        if (ArrayUtils.getLength(args) != 1) {
            return;
        }
        // Set server markers
        if (args[0] instanceof AsyncFrameBuffer) {
            AsyncFrameBuffer frameBuffer = (AsyncFrameBuffer) args[0];
            attachMarkersToInputProtocol(frameBuffer.getInputProtocol(), true);
        }

        final InterceptorScopeInvocation currentTransaction = this.scope.getCurrentInvocation();
        final Object attachment = currentTransaction.getOrCreateAttachment(ThriftClientCallContextAttachmentFactory.INSTANCE);
        if (attachment instanceof ThriftClientCallContext && target instanceof TBaseAsyncProcessor) {
            final ThriftClientCallContext clientCallContext = (ThriftClientCallContext) attachment;
            final String processName = toProcessName(target);
            clientCallContext.setProcessName(processName);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        // Unset server markers
        if (args[0] instanceof AsyncFrameBuffer) {
            AsyncFrameBuffer frameBuffer = (AsyncFrameBuffer) args[0];
            attachMarkersToInputProtocol(frameBuffer.getInputProtocol(), false);
        }
    }

    private boolean validateInputProtocol(Object iprot) {
        if (iprot instanceof TProtocol) {
            if (!(iprot instanceof ServerMarkerFlagFieldAccessor)) {
                if (isDebug) {
                    logger.debug("Invalid target object. Need field accessor({}).", ServerMarkerFlagFieldAccessor.class.getName());
                }
                return false;
            }
            if (!(iprot instanceof AsyncMarkerFlagFieldAccessor)) {
                if (isDebug) {
                    logger.debug("Invalid target object. Need field accessor({}).", AsyncMarkerFlagFieldAccessor.class.getName());
                }
                return false;
            }
            return true;
        }
        return false;
    }

    private void attachMarkersToInputProtocol(TProtocol iprot, boolean flag) {
        if (validateInputProtocol(iprot)) {
            ((ServerMarkerFlagFieldAccessor) iprot)._$PINPOINT$_setServerMarkerFlag(flag);
            ((AsyncMarkerFlagFieldAccessor) iprot)._$PINPOINT$_setAsyncMarkerFlag(flag);
        }
    }

    private String toProcessName(Object target) {
        return ThriftUtils.getAsyncProcessorNameAsUri((TBaseAsyncProcessor<?>) target);
    }
}
