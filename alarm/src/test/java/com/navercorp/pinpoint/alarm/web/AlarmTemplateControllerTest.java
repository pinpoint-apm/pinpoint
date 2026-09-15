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

import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.service.web.vo.ServiceConstants;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlarmTemplateControllerTest {

    @Test
    void updateTemplateReturnsConflictWhenDataSourceChangeIsRejected() throws Exception {
        String message = "Template item dataSource cannot be changed while rules reference it: itemId=10";
        AlarmTemplateService service = mock(AlarmTemplateService.class);
        doThrow(new AlarmResourceConflictException(message))
                .when(service)
                .updateTemplate(any(AlarmTemplate.class));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AlarmTemplateController(service))
                .setControllerAdvice(new AlarmExceptionHandler())
                .build();

        mockMvc.perform(put("/api/alarm/template/10")
                        .header(ServiceConstants.KEY, "service-a")
                        .param("applicationName", "test-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(templateBody()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Alarm resource conflict"))
                .andExpect(jsonPath("$.detail").value(message));
    }

    private static String templateBody() {
        return """
                {
                  "name": "template",
                  "description": "bundle",
                  "items": [
                    {
                      "id": 10,
                      "name": "item",
                      "severity": "WARNING",
                      "dataSource": "SECONDARY",
                      "checkIntervalSec": 300,
                      "actionIntervalSec": 1800,
                      "conditions": {
                        "type": "LEAF",
                        "metric": "total_count",
                        "op": ">=",
                        "threshold": 100.0,
                        "windowSec": 300,
                        "aggregation": "SUM"
                      },
                      "filters": []
                    }
                  ]
                }
                """;
    }
}
