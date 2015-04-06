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

import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.instrument.AttachmentFactory;
import com.navercorp.pinpoint.bootstrap.instrument.DefaultScopeDefinition;
import com.navercorp.pinpoint.bootstrap.instrument.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPoint;

public class ThreadLocalScopePoolTest {

    @Test
    public void testGetScope() throws Exception {

        ScopePool pool = new ThreadLocalScopePool();
        Scope scope = pool.getScope(new DefaultScopeDefinition("test"));
        Assert.assertTrue(scope instanceof ThreadLocalScope);

        Assert.assertEquals("name", scope.getName(), "test");
    }

    @Test
     public void testAttachment() throws Exception {

        ScopePool pool = new ThreadLocalScopePool();
        Scope scope = pool.getScope(new DefaultScopeDefinition("test"));

        scope.tryBefore(ExecutionPoint.BOUNDARY);
        scope.tryBefore(ExecutionPoint.BOUNDARY);
        Assert.assertNull(scope.getAttachment());
        scope.setAttachment("test");
        scope.tryAfter(ExecutionPoint.BOUNDARY);
        Assert.assertEquals(scope.getAttachment(), "test");
        Assert.assertTrue(scope.tryAfter(ExecutionPoint.BOUNDARY));
        
        Assert.assertEquals("name", scope.getName(), "test");
    }


    @Test
    public void testGetOrCreate() throws Exception {
        ScopePool pool = new ThreadLocalScopePool();
        Scope scope= pool.getScope(new DefaultScopeDefinition("test"));
        
        scope.tryBefore(ExecutionPoint.BOUNDARY);
        scope.tryBefore(ExecutionPoint.BOUNDARY);

        Assert.assertNull(scope.getAttachment());
        Assert.assertEquals(scope.getOrCreateAttachment(new AttachmentFactory() {
            @Override
            public Object createAttachment() {
                return "test";
            };
        }), "test");
        
        scope.tryAfter(ExecutionPoint.BOUNDARY);
        Assert.assertEquals(scope.getAttachment(), "test");
        Assert.assertTrue(scope.tryAfter(ExecutionPoint.BOUNDARY));

        Assert.assertEquals("name", scope.getName(), "test");
    }
}