/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.grpc.client;

import com.navercorp.pinpoint.grpc.Header;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

public class SupportCommandCodeClientInterceptor implements ClientInterceptor {

    private final Set<Short> supportCommandCodes;

    public SupportCommandCodeClientInterceptor(Set<Short> supportCommandCodes) {
        this.supportCommandCodes = Objects.requireNonNull(supportCommandCodes, "supportCommandCodes");
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        final ClientCall<ReqT, RespT> clientCall = next.newCall(method, callOptions);
        final ClientCall<ReqT, RespT> forwardClientCall = new SimpleForwardingClientCall<ReqT, RespT>(clientCall) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {

                String supportCommandCode = joinCommandCode(supportCommandCodes, Header.SUPPORT_COMMAND_CODE_DELIMITER);

                headers.put(Header.SUPPORT_COMMAND_CODE, supportCommandCode);
                super.start(responseListener, headers);
            }

        };
        return forwardClientCall;
    }

    private String joinCommandCode(Collection<Short> supportCommandCodes, String separator) {
        StringJoiner joiner = new StringJoiner(separator);
        for (Short supportCommandCode : supportCommandCodes) {
            joiner.add(supportCommandCode.toString());
        }
        return joiner.toString();
    }

}
