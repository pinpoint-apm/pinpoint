/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.grpc;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.grpc.interceptor.client.ChannelNewCallInterceptor;
import com.navercorp.pinpoint.plugin.grpc.interceptor.server.CopyAsyncContextInterceptor;
import com.navercorp.pinpoint.plugin.grpc.interceptor.server.ServerHalfCloseListenerInterceptor;
import com.navercorp.pinpoint.plugin.grpc.interceptor.server.ServerListenerInterceptor;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class GrpcUtils {
    private GrpcUtils() {
    }

    public static void addNewCallMethodInterceptor(InstrumentClass target) throws InstrumentException {
        InstrumentMethod newCallMethod = target.getDeclaredMethod("newCall", "io.grpc.MethodDescriptor", "io.grpc.CallOptions");
        if (newCallMethod != null) {
            newCallMethod.addInterceptor(ChannelNewCallInterceptor.class);
        } else {
            PLogger logger = PLoggerFactory.getLogger(GrpcUtils.class.getName());
            logger.debug("can't find newCall method");
        }
    }

    public static void addStartCallMethodInterceptor(InstrumentClass target) throws InstrumentException {
        InstrumentMethod startCall = target.getDeclaredMethod("startCall", "io.grpc.ServerCall", "io.grpc.Metadata");
        if (startCall != null) {
            startCall.addInterceptor(CopyAsyncContextInterceptor.class);
        } else {
            PLogger logger = PLoggerFactory.getLogger(GrpcUtils.class.getName());
            logger.debug("can't find startCall method");
        }
    }

    public static void addServerListenerMethod(InstrumentClass target, boolean traceOnMessage) throws InstrumentException {
        List<InstrumentMethod> declaredMethods = target.getDeclaredMethods();
        for (InstrumentMethod declaredMethod : declaredMethods) {
            if (declaredMethod.getName().equals("onMessage") && !traceOnMessage) {
                PLogger logger = PLoggerFactory.getLogger(GrpcUtils.class.getName());
                logger.debug("skip add onMessage interceptor");
                continue;
            }

            if (declaredMethod.getName().equals("onHalfClose")) {
                declaredMethod.addInterceptor(ServerHalfCloseListenerInterceptor.class);
                continue;
            }

            declaredMethod.addInterceptor(ServerListenerInterceptor.class);
        }

    }
}
