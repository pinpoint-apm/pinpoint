/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdk.http.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.jdk.http.DelegateGetter;
import sun.net.www.protocol.https.DelegateHttpsURLConnection;

public class HttpsURLConnectionImplInterceptor extends AbstractHttpURLConnectionInterceptor {

    public HttpsURLConnectionImplInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        super(traceContext, descriptor, scope);
    }

    @Override
    boolean isConnected(final Object target) {
        if (target instanceof DelegateGetter) {
            DelegateHttpsURLConnection delegateHttpsURLConnection = ((DelegateGetter) target)._$PINPOINT$_getDelegate();
            if (delegateHttpsURLConnection != null) {
                return delegateHttpsURLConnection.isConnected();
            }
        }
        return false;
    }
}
