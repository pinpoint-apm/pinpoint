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

import junit.framework.Assert;
import net.spy.memcached.ops.OperationState;
import org.junit.Test;

public class BaseOperationTransitionStateInterceptorTest {

    @Test
    public void testComplete() throws Exception {
        // 타입비교를 Arcus의 경우 TIMEDOUT state가 별도로 추가되어 정적 타입비교를 할수 있는 상황이 아님.
        // toString()을 호출하여, 문자열 비교를 해야 함.
        String complete = OperationState.COMPLETE.toString();
        Assert.assertEquals("COMPLETE", complete);
    }

    @Test
    public void existArcusTimeoutState() throws Exception {
        // 클래스가 강제 로딩되서 다른 test에 영향을 줄수 있음.
        if (!isArcusExist()) {
            // arcus만의 state체크를 위한 것이므로 없으면 패스한다.
            return;
        }
        // Arcus OperationState.timedout에 변경이 있는지 체크한다.
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
