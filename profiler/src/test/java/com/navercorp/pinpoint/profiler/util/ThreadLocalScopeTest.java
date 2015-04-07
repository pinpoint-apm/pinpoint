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

/**
 * @author emeroad
 */
import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.instrument.DefaultScopeDefinition;
import com.navercorp.pinpoint.bootstrap.instrument.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPoint;

/**
 * @author emeroad
 */
public class ThreadLocalScopeTest {
    @Test
    public void pushPop() {
        Scope scope = new ThreadLocalScope(new DefaultScopeDefinition("test"));
        Assert.assertTrue(scope.tryEnter(ExecutionPoint.BOUNDARY));
        scope.entered(ExecutionPoint.BOUNDARY);
        Assert.assertFalse(scope.tryEnter(ExecutionPoint.BOUNDARY));
        Assert.assertFalse(scope.tryEnter(ExecutionPoint.BOUNDARY));
        
        Assert.assertTrue(scope.isIn());

        Assert.assertFalse(scope.tryLeave(ExecutionPoint.BOUNDARY));
        Assert.assertFalse(scope.tryLeave(ExecutionPoint.BOUNDARY));
        Assert.assertTrue(scope.tryLeave(ExecutionPoint.BOUNDARY));
        scope.leaved(ExecutionPoint.BOUNDARY);
    }

    @Test(expected=IllegalStateException.class)
    public void pushPopError() {
        Scope scope = new ThreadLocalScope(new DefaultScopeDefinition("test"));
        scope.leaved(ExecutionPoint.BOUNDARY);
    }

    @Test
    public void getName() {
        Scope scope = new ThreadLocalScope(new DefaultScopeDefinition("test"));
        Assert.assertEquals(scope.getName(), "test");

    }
}

