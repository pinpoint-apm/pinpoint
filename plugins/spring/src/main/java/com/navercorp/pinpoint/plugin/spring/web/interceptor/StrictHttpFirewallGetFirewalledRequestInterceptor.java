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

package com.navercorp.pinpoint.plugin.spring.web.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;

/**
 * @author jaehong.kim
 */
public class StrictHttpFirewallGetFirewalledRequestInterceptor implements AroundInterceptor {
    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        AsyncContextAccessor accessor = ArrayArgumentUtils.getArgument(args, 0, AsyncContextAccessor.class);
        if (accessor != null) {
            final AsyncContext asyncContext = accessor._$PINPOINT$_getAsyncContext();
            if (asyncContext != null && result instanceof AsyncContextAccessor) {
                // StrictHttpFirewall$StrictFirewalledRequest
                ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(asyncContext);
            }
        }
    }
}
