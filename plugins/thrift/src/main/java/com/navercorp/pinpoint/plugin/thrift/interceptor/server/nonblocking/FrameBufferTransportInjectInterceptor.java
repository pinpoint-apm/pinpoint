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

package com.navercorp.pinpoint.plugin.thrift.interceptor.server.nonblocking;

import java.net.Socket;

import com.navercorp.pinpoint.bootstrap.FieldAccessor;
import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.plugin.thrift.ThriftConstants;

/**
 * Base interceptor for retrieving the socket information from a TTransport field, and injecting it into a transport wrapping it.
 * 
 * @author HyunGil Jeong
 */
public abstract class FrameBufferTransportInjectInterceptor implements SimpleAroundInterceptor, ThriftConstants {

    private final MetadataAccessor socketAccessor;
    private final FieldAccessor transFieldAccessor;
    
    protected FrameBufferTransportInjectInterceptor(
            MetadataAccessor socketAccessor,
            FieldAccessor transFieldAccessor) {
        this.socketAccessor = socketAccessor;
        this.transFieldAccessor = transFieldAccessor;
    }

    // Retrieve the socket information from the trans_ field of the given instance.
    protected final Socket getRootSocket(Object target) {
        if (this.transFieldAccessor.isApplicable(target)) {
            Object trans = this.transFieldAccessor.get(target);
            if (trans != null && this.socketAccessor.isApplicable(trans)) {
                Socket rootSocket = this.socketAccessor.get(trans);
                return rootSocket;
            }
        }
        return null;
    }
    
    // Inject the socket information into the given memory-based transport
    protected final void injectSocket(Object inTrans, Socket rootSocket) {
        if (inTrans != null && this.socketAccessor.isApplicable(inTrans)) {
            this.socketAccessor.set(inTrans, rootSocket);
        }
    }
}
