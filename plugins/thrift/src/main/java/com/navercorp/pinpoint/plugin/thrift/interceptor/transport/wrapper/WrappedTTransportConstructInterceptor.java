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

package com.navercorp.pinpoint.plugin.thrift.interceptor.transport.wrapper;

import java.net.Socket;

import org.apache.thrift.transport.TTransport;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.thrift.ThriftConstants;

/**
 * @author HyunGil Jeong
 */
public abstract class WrappedTTransportConstructInterceptor implements SimpleAroundInterceptor, ThriftConstants {

    private final MetadataAccessor socketAccessor;
    
    protected WrappedTTransportConstructInterceptor(
            @Name(METADATA_SOCKET) MetadataAccessor socketAccessor) {
        this.socketAccessor = socketAccessor;
    }

    @Override
    public final void before(Object target, Object[] args) {
        // Do nothing
    }

    @Override
    public final void after(Object target, Object[] args, Object result, Throwable throwable) {
        TTransport wrappedTransport = getWrappedTransport(args);
        if (wrappedTransport != null && this.socketAccessor.isApplicable(wrappedTransport)) {
            Socket rootSocket = this.socketAccessor.get(wrappedTransport);
            if (rootSocket != null) {
                this.socketAccessor.set(target, rootSocket);
            }
        }
    }
    
    protected abstract TTransport getWrappedTransport(Object[] args);

}
