/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.trace.ServiceType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

public class TraceIndexRowKeyUtilsTest {

    private static final int SALT_KEY_SIZE = 1;

    @Test
    public void rowValueExtractTest() {
        String applicationName = "testApp";
        long timestamp = 1234567890L;
        long spanId = 9876543210L;
        byte[] rowKey = TraceIndexRowKeyUtils.createRowKeyWithSaltSize(
                SALT_KEY_SIZE, 0, applicationName, ServiceType.TEST.getCode(), timestamp,
                9876543210L, 100, 0, "agentId"
        );

        Assertions.assertThat(TraceIndexRowKeyUtils.extractAcceptTime(rowKey, 0)).isEqualTo(timestamp);
        Assertions.assertThat(TraceIndexRowKeyUtils.extractSpanId(rowKey, 0)).isEqualTo(spanId);
        Assertions.assertThat(TraceIndexRowKeyUtils.extractApplicationName(rowKey, 0)).isEqualTo(applicationName);

        Predicate<byte[]> applicationNamePredicate = TraceIndexRowKeyUtils.createApplicationNamePredicate(applicationName);
        Assertions.assertThat(applicationNamePredicate.test(rowKey)).isEqualTo(true);
    }
}
