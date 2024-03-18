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

package com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayUtils;

/**
 * @author HyunGil Jeong
 */
public class QueueingConsumerHandleInterceptor implements AroundInterceptor {

    @Override
    public void before(Object target, Object[] args) {
        // do nothing
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (ArrayUtils.isEmpty(args)) {
            return;
        }
        if (!(args[0] instanceof AsyncContextAccessor)) {
            return;
        }
        if (!(result instanceof AsyncContextAccessor)) {
            return;
        }
        AsyncContext asyncContext = ((AsyncContextAccessor) args[0])._$PINPOINT$_getAsyncContext();
        ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(asyncContext);
    }
}
