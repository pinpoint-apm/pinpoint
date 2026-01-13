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

package com.navercorp.pinpoint.web.trace.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static com.navercorp.pinpoint.web.trace.view.TransactionCallTreeViewModel.Field;

class TransactionInfoViewModelTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    void fieldName() throws JsonProcessingException {
        Field.CallStackMeta callStackMeta = Field.getCallStackMeta();

        ObjectMapper mapper = Jackson.newMapper();
        String json = mapper.writeValueAsString(callStackMeta);

        logger.debug("{}", json);
    }
}