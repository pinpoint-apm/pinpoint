/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.kafka;

import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;

import java.util.Properties;

public abstract class KafkaClient2ITBase {
    protected static final Logger logger = LogManager.getLogger(KafkaClient2ITBase.class);

    static String brokerUrl;
    static int PORT;
    static long offset;

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
    }

    @BeforeAll
    public static void beforeAll() {
        PORT = Integer.parseInt(System.getProperty("PORT"));
        brokerUrl = "localhost:" + PORT;
        offset = Long.parseLong(System.getProperty("OFFSET"));
    }

    public static int getPort() {
        return PORT;
    }

    public static long getOffset() {
        return offset;
    }
}
