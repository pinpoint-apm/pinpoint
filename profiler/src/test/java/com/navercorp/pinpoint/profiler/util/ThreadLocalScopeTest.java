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
        Assert.assertTrue(scope.tryBefore(ExecutionPoint.BOUNDARY));
        Assert.assertFalse(scope.tryBefore(ExecutionPoint.BOUNDARY));
        Assert.assertFalse(scope.tryBefore(ExecutionPoint.BOUNDARY));
        
        Assert.assertTrue(scope.isIn());

        Assert.assertFalse(scope.tryAfter(ExecutionPoint.BOUNDARY));
        Assert.assertFalse(scope.tryAfter(ExecutionPoint.BOUNDARY));
        Assert.assertTrue(scope.tryAfter(ExecutionPoint.BOUNDARY));
    }

    @Test(expected=IllegalStateException.class)
    public void pushPopError() {
        Scope scope = new ThreadLocalScope(new DefaultScopeDefinition("test"));
        scope.tryAfter(ExecutionPoint.BOUNDARY);
    }

    @Test
    public void getName() {
        Scope scope = new ThreadLocalScope(new DefaultScopeDefinition("test"));
        Assert.assertEquals(scope.getName(), "test");

    }
}

