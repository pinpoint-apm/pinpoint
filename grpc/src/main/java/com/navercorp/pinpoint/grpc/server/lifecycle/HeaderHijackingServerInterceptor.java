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

package com.navercorp.pinpoint.grpc.server.lifecycle;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class HeaderHijackingServerInterceptor implements ServerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LifecycleRegistry lifecycleRegistry;
    private final LifecycleListener lifecycleListener;

    public HeaderHijackingServerInterceptor(LifecycleRegistry lifecycleRegistry, LifecycleListener lifecycleListener) {
        this.lifecycleRegistry = Assert.requireNonNull(lifecycleRegistry, "lifecycleRegistry must not be null");
        this.lifecycleListener = Assert.requireNonNull(lifecycleListener, "lifecycleListener must not be null");
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        final Context context = Context.current();
        final TransportMetadata transportMetadata = ServerContext.getTransportMetadata(context);

        final Lifecycle lifecycle = lifecycleRegistry.get(transportMetadata.getTransportId());
        final AgentHeaderFactory.Header firstSetting = lifecycle.getRef();
        if (firstSetting == null) {
            final AgentHeaderFactory.Header agentInfo = ServerContext.getAgentInfo(context);
            if (lifecycle.setInitialRef(agentInfo)) {
                if (logger.isInfoEnabled()) {
                    logger.info("setInitialRef:{}", lifecycle);
                }
                lifecycleListener.handshake(lifecycle);
            }
        } else {
            if (logger.isDebugEnabled()) {
                AgentHeaderFactory.Header agentInfo = ServerContext.getAgentInfo(context);
                if (!agentInfo.equals(firstSetting)) {
                    logger.warn("Unexpected header changed. agentInfo:{}, first:{}", agentInfo, firstSetting);
//                    TODO close ??
//                    Status.FAILED_PRECONDITION.withDescription("Unexpected header changed");
                }
            }

        }

        return next.startCall(call, headers);
    }
}
