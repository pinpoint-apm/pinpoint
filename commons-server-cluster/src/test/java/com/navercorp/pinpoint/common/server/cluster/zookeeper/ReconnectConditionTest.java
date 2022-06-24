/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.cluster.zookeeper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Taejin Koo
 */
public class ReconnectConditionTest {

    @Disabled
    @Test
    public void functionTest() {
        ReconnectCondition condition = new ReconnectCondition(1, 1);

        NotConnectedStatus notConnectedStatus = new NotConnectedStatus();

        while (true) {
            if (condition.check(notConnectedStatus)) {
                notConnectedStatus.reset();
                System.out.println("BREAK");
                break;
            }
            doSleep1Sec();
            notConnectedStatus.update();
        }

        while (true) {
            if (condition.check(notConnectedStatus)) {
                System.out.println("BREAK");
                break;
            }
            doSleep1Sec();
            notConnectedStatus.update();
        }
        System.out.println("END");

    }

    private void doSleep1Sec() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }
}
