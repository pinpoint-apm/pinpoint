/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class DotSerializerTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectMapper mapper = Jackson.newMapper();

    @Test
    public void testSerialize() throws Exception {
        TransactionId transactionId = TransactionIdUtils.parseTransactionId("aigw.dev.1^1395798795017^1527177");
        Dot dot = new Dot(transactionId, 100, 99, 1, "agent");
        String jsonValue = mapper.writeValueAsString(dot);
        Assertions.assertEquals("[100,99,\"aigw.dev.1^1395798795017^1527177\",0]", jsonValue);
    }
}
