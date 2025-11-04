/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertViewServiceTest {

    @Test
    void calculatesErrorRateCorrectlyForValidInputs() {
        AlertViewService alertService = new AlertViewService();
        assertEquals(50.0, alertService.getErrorRate(200, 100));
        assertEquals(25.0, alertService.getErrorRate(400, 100));
    }

    @Test
    void returnsFalseWhenErrorRateIsBelowDefaultThreshold() {
        AlertViewService alertService = new AlertViewService();
        assertFalse(alertService.hasAlert(100, 9));
    }

    @Test
    void handlesCustomThresholdCorrectly() {
        AlertViewService alertService = new AlertViewService(20.0);
        assertTrue(alertService.hasAlert(100, 21));
        assertFalse(alertService.hasAlert(100, 19));
    }

    @Test
    void hasAlert() {
        AlertViewService alertService = new AlertViewService(10.0);
        assertTrue(alertService.hasAlert(100, 10));
        assertFalse(alertService.hasAlert(100, 0));
    }

    @Test
    void handlesNegativeTotalCountGracefully() {
        AlertViewService alertService = new AlertViewService();
        assertFalse(alertService.hasAlert(-100, 10));
    }

    @Test
    void handlesNegativeThresholdGracefully() {
        AlertViewService alertService = new AlertViewService(-5.0);
        assertTrue(alertService.hasAlert(100, 1));
    }

}