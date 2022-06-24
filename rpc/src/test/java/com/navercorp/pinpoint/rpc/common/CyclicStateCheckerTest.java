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

package com.navercorp.pinpoint.rpc.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Taejin Koo
 */
public class CyclicStateCheckerTest {

    @Test
    public void stateCheckerTest1() {
        CyclicStateChecker stateChecker = new CyclicStateChecker(3);

        stateChecker.markAndCheckCondition();
        Assertions.assertFalse(stateChecker.checkCondition());

        stateChecker.markAndCheckCondition();
        Assertions.assertFalse(stateChecker.checkCondition());

        stateChecker.markAndCheckCondition();
        Assertions.assertTrue(stateChecker.checkCondition());

        stateChecker.markAndCheckCondition();
        Assertions.assertTrue(stateChecker.checkCondition());
    }

    @Test
    public void stateCheckerTest2() {
        CyclicStateChecker stateChecker = new CyclicStateChecker(3);

        stateChecker.markAndCheckCondition();
        Assertions.assertFalse(stateChecker.checkCondition());

        stateChecker.markAndCheckCondition();
        Assertions.assertFalse(stateChecker.checkCondition());

        stateChecker.unmark();
        Assertions.assertFalse(stateChecker.checkCondition());

        stateChecker.markAndCheckCondition();
        Assertions.assertFalse(stateChecker.checkCondition());

        stateChecker.markAndCheckCondition();
        Assertions.assertFalse(stateChecker.checkCondition());

        stateChecker.markAndCheckCondition();
        Assertions.assertTrue(stateChecker.checkCondition());
    }

}