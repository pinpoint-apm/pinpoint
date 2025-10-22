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

package com.navercorp.pinpoint.common.trace;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class BaseHistogramSchemaTest {

    @Test
    void findHistogramSlot() {
        HistogramSchema schema = HistogramSchemas.NORMAL_SCHEMA;

        HistogramSlot fastSuccess = schema.findHistogramSlot(1000, false);
        Assertions.assertEquals(schema.getFastSlot(), fastSuccess);

        HistogramSlot fastFailed = schema.findHistogramSlot(1000, true);
        Assertions.assertEquals(schema.getFastErrorSlot(), fastFailed);
    }
}