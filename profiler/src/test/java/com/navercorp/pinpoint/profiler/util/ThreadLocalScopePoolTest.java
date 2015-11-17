/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.instrument.DefaultInterceptorScopeDefinition;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.AttachmentFactory;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;

public class ThreadLocalScopePoolTest {

    @Test
    public void testGetScope() throws Exception {

        ScopePool pool = new ThreadLocalScopePool();
        InterceptorScopeInvocation scope = pool.getScope(new DefaultInterceptorScopeDefinition("test"));
        Assert.assertTrue(scope instanceof ThreadLocalScope);

        Assert.assertEquals("name", scope.getName(), "test");
    }

    @Test
     public void testAttachment() throws Exception {

        ScopePool pool = new ThreadLocalScopePool();
        InterceptorScopeInvocation scope = pool.getScope(new DefaultInterceptorScopeDefinition("test"));

        scope.tryEnter(ExecutionPolicy.BOUNDARY);
        scope.tryEnter(ExecutionPolicy.BOUNDARY);
        
        Assert.assertNull(scope.getAttachment());
        scope.setAttachment("test");
        
        scope.canLeave(ExecutionPolicy.BOUNDARY);
        Assert.assertEquals(scope.getAttachment(), "test");
        
        Assert.assertTrue(scope.canLeave(ExecutionPolicy.BOUNDARY));
        scope.leave(ExecutionPolicy.BOUNDARY);
        
        Assert.assertEquals("name", scope.getName(), "test");
    }


    @Test
    public void testGetOrCreate() throws Exception {
        ScopePool pool = new ThreadLocalScopePool();
        InterceptorScopeInvocation scope= pool.getScope(new DefaultInterceptorScopeDefinition("test"));
        
        scope.tryEnter(ExecutionPolicy.BOUNDARY);
        scope.tryEnter(ExecutionPolicy.BOUNDARY);

        Assert.assertNull(scope.getAttachment());
        Assert.assertEquals(scope.getOrCreateAttachment(new AttachmentFactory() {
            @Override
            public Object createAttachment() {
                return "test";
            }
        }), "test");
        
        scope.canLeave(ExecutionPolicy.BOUNDARY);
        Assert.assertEquals(scope.getAttachment(), "test");
        Assert.assertTrue(scope.canLeave(ExecutionPolicy.BOUNDARY));
        scope.leave(ExecutionPolicy.BOUNDARY);
        
        Assert.assertEquals("name", scope.getName(), "test");
    }
}