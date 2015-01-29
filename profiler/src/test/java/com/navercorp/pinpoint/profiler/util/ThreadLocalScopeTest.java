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
import com.navercorp.pinpoint.bootstrap.instrument.Scope;
import org.junit.Test;
import org.junit.Assert;

/**
 * @author emeroad
 */
public class ThreadLocalScopeTest {
    @Test
    public void pushPop() {
        Scope scope = new ThreadLocalScope(new SimpleScopeFactory("test"));
        Assert.assertEquals(scope.push(), 0);
        Assert.assertEquals(scope.push(), 1);
        Assert.assertEquals(scope.push(), 2);

        Assert.assertEquals(scope.depth(), 3);

        Assert.assertEquals(scope.pop(), 2);
        Assert.assertEquals(scope.pop(), 1);
        Assert.assertEquals(scope.pop(), 0);
    }

    @Test
    public void pushPopError() {
        Scope scope = new ThreadLocalScope(new SimpleScopeFactory("test"));
        Assert.assertEquals(scope.pop(), -1);
        Assert.assertEquals(scope.pop(), -2);

        Assert.assertEquals(scope.push(), -2);
        Assert.assertEquals(scope.push(), -1);

        Assert.assertEquals(scope.depth(), 0);


    }

    @Test
    public void getName() {
        Scope scope = new ThreadLocalScope(new SimpleScopeFactory("test"));
        Assert.assertEquals(scope.getName(), "test");

    }
}

