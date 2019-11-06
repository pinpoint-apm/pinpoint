/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.hystrix.interceptor.metrics;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.plugin.hystrix.field.HystrixKeyNameAccessor;

/**
 * @author HyunGil Jeong
 */
public class HystrixKeyConstructInterceptor implements AroundInterceptor {

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (target instanceof HystrixKeyNameAccessor) {
            if (args != null && args.length > 0) {
                Object name = args[0];
                if (name instanceof String) {
                    ((HystrixKeyNameAccessor) target)._$PINPOINT$_setHystrixKeyName((String) name);
                }
            }
        }
    }
}
