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
package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.vo.TestAlarmDataSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlarmCatalogControllerTest {

    private static List<String> values(List<AlarmCatalogResponse.DataSource> dataSources) {
        return dataSources.stream().map(AlarmCatalogResponse.DataSource::value).toList();
    }

    @Test
    void anApplicationTypeLimitsTheDataSourcesToItsCategory() {
        AlarmApplicationResolver resolver = mock(AlarmApplicationResolver.class);
        when(resolver.categoryOf("javascript")).thenReturn("OTHER");
        when(resolver.categoryOf("SPRING_BOOT")).thenReturn(AlarmDataSource.APM_CATEGORY);
        AlarmCatalogController controller = new AlarmCatalogController(
                mock(AlarmTemplatePresetLoader.class), AlarmServiceTestSupport.DATA_SOURCE_REGISTRY, resolver);

        assertEquals(List.of(TestAlarmDataSource.OTHER_CATEGORY.name()), values(controller.getDataSources("javascript")));
        assertEquals(List.of(TestAlarmDataSource.AGENT_STAT.name(), TestAlarmDataSource.APPLICATION_RESPONSE.name()),
                values(controller.getDataSources("SPRING_BOOT")));
        assertEquals(3, controller.getDataSources(null).size());
    }
}
