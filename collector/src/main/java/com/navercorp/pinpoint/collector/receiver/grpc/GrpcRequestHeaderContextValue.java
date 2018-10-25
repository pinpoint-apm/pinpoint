/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.grpc;

import io.grpc.Context;

/**
 * @author jaehong.kim
 */
public class GrpcRequestHeaderContextValue {

    private static final Context.Key<GrpcRequestHeader> REQUEST_HEADER_KEY = Context.key("REQUEST_HEADER");

    public static GrpcRequestHeader get() {
        return REQUEST_HEADER_KEY.get();
    }

    private GrpcRequestHeaderContextValue() {
    }

    public static class ContextBuilder {
        private GrpcRequestHeader requestHeader;

        public void setRequestHeader(GrpcRequestHeader requestHeader) {
            this.requestHeader = requestHeader;
        }

        public Context build() {
            return Context.current().withValue(REQUEST_HEADER_KEY, this.requestHeader);
        }
    }
}