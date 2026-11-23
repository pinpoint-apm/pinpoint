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

package com.navercorp.pinpoint.it.plugin.rabbitmq.spring;

import com.navercorp.pinpoint.test.plugin.api.SharedTestBeforeAllResult;
import org.junit.jupiter.api.BeforeAll;
import test.pinpoint.plugin.rabbitmq.spring.config.CommonConfig;

import java.util.Properties;

public class SpringAmqpRabbitITBase {

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
    }

    @BeforeAll
    public static void beforeAll() {
        int port = Integer.parseInt(System.getProperty("PORT"));
        CommonConfig.setPort(port);
    }
}
