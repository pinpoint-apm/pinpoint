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

package com.navercorp.pinpoint.profiler.modifier.arcus.interceptor;

import org.junit.Assert;
import net.spy.memcached.ops.OperationState;
import org.junit.Test;

public class BaseOperationTransitionStateInterceptorTest {

    @Test
    public void testComplete() throws Exception {
        // Arcus added TIMEOUT to OperationState of memcached. 
        // So you cannot just compare enum values but have to compare by their string representation.
        String complete = OperationState.COMPLETE.toString();
        Assert.assertEquals("COMPLETE", complete);
    }

    @Test
    public void existArcusTimeoutState() throws Exception {
        // Could affects other tests because this test forces to load a class
        if (!isArcusExist()) {
            // Skip test if Arcus is not present.
            return;
        }
        
        // Test if OperationState contains TIMEDOUT value.
        OperationState[] values = OperationState.values();
        for (OperationState value : values) {
            if (value.toString().equals("TIMEDOUT")) {
                return;
            }
        }

        Assert.fail("OperationState.TIMEDOUT state not found");
    }

    private boolean isArcusExist() {
        try {
            Class.forName("net.spy.memcached.ArcusClient");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
