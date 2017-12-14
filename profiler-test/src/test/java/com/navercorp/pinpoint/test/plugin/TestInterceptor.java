/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Scope;

@Scope(DefaultClassEditorBuilderTest.SCOPE_NAME)
public class TestInterceptor implements AroundInterceptor {
    private final String field;
    
    public TestInterceptor(String field) {
        this.field = field;
    }

    @Override
    public void before(Object target, Object[] args) {
        // TODO Auto-generated method stub

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        // TODO Auto-generated method stub

    }

}
