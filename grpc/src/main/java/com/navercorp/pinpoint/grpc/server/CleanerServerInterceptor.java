/*
 * Copyright 2022 NAVER Corp.
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
package com.navercorp.pinpoint.grpc.server;

import io.grpc.*;

/**
 * @author youngjin.kim2
 */
public class CleanerServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        final Attributes attributes = call.getAttributes();

        final TransportCleaner cleaner = attributes.get(CleanerServerTransportFilter.TRANSPORT_CLEANER_KEY);
        if (cleaner == null) {
            call.close(Status.INTERNAL.withDescription("transportCleaner is null"), new Metadata());
            return new ServerCall.Listener<ReqT>() {
            };
        }

        final Context currentContext = Context.current();
        final Context newContext = currentContext.withValue(ServerContext.getTransportCleanerKey(), cleaner);
        return Contexts.interceptCall(newContext, call, headers, next);
    }

}
