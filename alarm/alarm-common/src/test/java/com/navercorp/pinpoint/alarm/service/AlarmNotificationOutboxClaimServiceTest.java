/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.alarm.service;

import com.navercorp.pinpoint.alarm.dao.AlarmNotificationOutboxDao;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class AlarmNotificationOutboxClaimServiceTest {

    @Test
    void aProcessEvaluatingNothingIsRefusedAtStartup() {
        // The claim query names the data sources in an IN list, which cannot be empty.
        assertThrows(IllegalStateException.class, () -> new AlarmNotificationOutboxClaimService(
                mock(AlarmNotificationOutboxDao.class), mock(PlatformTransactionManager.class), Set.of()));
    }
}
