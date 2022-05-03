/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.openwhisk.interceptor;

import akka.http.scaladsl.server.RequestContextImpl;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;

/**
 * @author Seonghyun Oh
 */
public class TransactionIdCreateInterceptor implements AroundInterceptor {


    public TransactionIdCreateInterceptor() {
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

        int requestContextIndex = getArgsIndexOfRequestContext(args);
        if (requestContextIndex == -1) {
            return;
        }
        AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args, requestContextIndex);
        if (asyncContext == null) {
            return;
        }
        ((AsyncContextAccessor) (result))._$PINPOINT$_setAsyncContext(asyncContext);
    }

    private int getArgsIndexOfRequestContext(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof RequestContextImpl) return i;
        }
        return -1;
    }

}

