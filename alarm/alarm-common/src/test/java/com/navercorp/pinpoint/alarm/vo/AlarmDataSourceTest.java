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

package com.navercorp.pinpoint.alarm.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlarmDataSourceTest {

    // A data source written before the service was passed keeps its link.
    @Test
    void theLinkWithTheServiceFallsBackToTheLinkWithout() {
        AlarmDataSource dataSource = mock(AlarmDataSource.class, CALLS_REAL_METHODS);
        when(dataSource.detailLink("https://p", "app@T", 1L, 2L)).thenReturn("https://p/old/app@T");

        assertEquals("https://p/old/app@T", dataSource.detailLink("https://p", "svc", "app@T", 1L, 2L));
    }
}
