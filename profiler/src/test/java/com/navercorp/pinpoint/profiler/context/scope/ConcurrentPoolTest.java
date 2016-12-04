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

package com.navercorp.pinpoint.profiler.context.scope;

import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ConcurrentPoolTest {
    @Test
    public void testConcurrentPool() throws Exception {

        InterceptorScopeFactory traceScopeFactory = new InterceptorScopeFactory();
        Pool<String, InterceptorScope> pool = new ConcurrentPool<String, InterceptorScope>(traceScopeFactory);

        final String OBJECT_NAME = "test";

        InterceptorScope addedScope = pool.get(OBJECT_NAME);
        Assert.assertSame(pool.get(OBJECT_NAME), addedScope);

        InterceptorScope exist = pool.get(OBJECT_NAME);
        Assert.assertSame(exist, addedScope);

        InterceptorScope another = pool.get("another");
        Assert.assertNotSame(exist, another);
    }

}