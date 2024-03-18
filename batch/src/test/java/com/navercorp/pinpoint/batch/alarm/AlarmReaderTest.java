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

package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.service.AlarmService;
import com.navercorp.pinpoint.web.service.ApplicationService;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepExecution;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AlarmReaderTest {

    @Mock
    private ApplicationService applicationService;

    @Mock
    private AlarmService alarmService;

    @Mock
    private StepExecution stepExecution;

    private static final List<Application> mockApplications = List.of(
            new Application(ApplicationId.of(UUID.randomUUID()), "testApplication0", ServiceType.TEST),
            new Application(ApplicationId.of(UUID.randomUUID()), "testApplication1", ServiceType.TEST),
            new Application(ApplicationId.of(UUID.randomUUID()), "testApplication2", ServiceType.TEST),
            new Application(ApplicationId.of(UUID.randomUUID()), "testApplication3", ServiceType.TEST)
    );

    private static final List<String> applicationIds = mockApplications.stream()
            .map(Application::name)
            .toList();

    @Test
    public void pollingTest() {
        when(applicationService.getApplications()).thenReturn(mockApplications);
        when(alarmService.selectApplicationId()).thenReturn(applicationIds);

        AlarmReader reader = new AlarmReader(applicationService, alarmService);
        reader.beforeStep(stepExecution);
        for (int i = 0; i < 4; i++) {
            assertEquals(mockApplications.get(i), reader.read(), "polled application should be same");
        }
        assertNull(reader.read());
    }

    @Test
    public void pollingFromEmptyTest() {
        when(applicationService.getApplications()).thenReturn(List.of());

        AlarmReader reader = new AlarmReader(applicationService, alarmService);
        reader.beforeStep(stepExecution);
        assertNull(reader.read());
    }
}
