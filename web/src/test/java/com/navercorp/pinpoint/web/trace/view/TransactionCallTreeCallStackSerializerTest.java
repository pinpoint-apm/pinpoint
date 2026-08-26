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
package com.navercorp.pinpoint.web.trace.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.trace.callstacks.ErrorKey;
import com.navercorp.pinpoint.web.trace.callstacks.Record;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionCallTreeCallStackSerializerTest {

    private static final int ERROR_KEY_INDEX = TransactionCallTreeViewModel.Field.errorKey.ordinal();

    private final ObjectMapper mapper = new ObjectMapper();

    private Record mockRecord() {
        Record record = mock(Record.class);
        when(record.getMethodTypeEnum()).thenReturn(MethodTypeEnum.DEFAULT);
        return record;
    }

    @Test
    public void serialize_errorKey_asObject() throws Exception {
        Record record = mockRecord();
        when(record.getErrorKey()).thenReturn(new ErrorKey(ServiceUid.DEFAULT_SERVICE_UID_NAME, "app-name", "agent-id", "agent-id^0^30", 100L, 1400L));

        TransactionCallTreeViewModel.CallStack callStack = new TransactionCallTreeViewModel.CallStack(record, 0, -1);
        JsonNode row = mapper.readTree(mapper.writeValueAsString(callStack));

        JsonNode errorKey = row.get(ERROR_KEY_INDEX);
        assertThat(errorKey.isObject()).isTrue();
        assertThat(errorKey.get("serviceName").asText()).isEqualTo(ServiceUid.DEFAULT_SERVICE_UID_NAME);
        assertThat(errorKey.get("applicationName").asText()).isEqualTo("app-name");
        assertThat(errorKey.get("agentId").asText()).isEqualTo("agent-id");
        assertThat(errorKey.get("transactionId").asText()).isEqualTo("agent-id^0^30");
        assertThat(errorKey.get("spanId").isTextual()).isTrue();
        assertThat(errorKey.get("spanId").asText()).isEqualTo("100");
        assertThat(errorKey.get("exceptionId").isTextual()).isTrue();
        assertThat(errorKey.get("exceptionId").asText()).isEqualTo("1400");
    }

    @Test
    public void serialize_withoutErrorKey_writesNull() throws Exception {
        Record record = mockRecord();

        TransactionCallTreeViewModel.CallStack callStack = new TransactionCallTreeViewModel.CallStack(record, 0, -1);
        JsonNode row = mapper.readTree(mapper.writeValueAsString(callStack));

        assertThat(row.get(ERROR_KEY_INDEX).isNull()).isTrue();
    }
}
