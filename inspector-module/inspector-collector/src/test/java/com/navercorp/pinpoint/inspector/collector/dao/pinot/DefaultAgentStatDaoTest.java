/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.collector.dao.pinot;

import org.apache.kafka.common.utils.Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author minwoo-jung
 */
class DefaultAgentStatDaoTest {

    @Test
    public void patitionTest() {
        int numPartitions = 64;
        String key = "minwoo_local_app#jvmGc";
        int partition = Utils.toPositive(Utils.murmur2(key.getBytes())) % numPartitions;
        assertEquals(18, partition);
    }

}