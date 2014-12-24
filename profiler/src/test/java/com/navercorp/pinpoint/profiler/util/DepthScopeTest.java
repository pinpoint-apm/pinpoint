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
import org.junit.Test;
import org.junit.Assert;

import com.navercorp.pinpoint.profiler.util.DepthScope;

/**
 * @author emeroad
 */
public class DepthScopeTest {
    @Test
    public void pushPop() {
        DepthScope scope = new DepthScope("test");
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
        DepthScope scope = new DepthScope("test");
        Assert.assertEquals(scope.pop(), -1);
        Assert.assertEquals(scope.pop(), -2);

        Assert.assertEquals(scope.push(), -2);
        Assert.assertEquals(scope.push(), -1);

        Assert.assertEquals(scope.depth(), 0);


    }
}

