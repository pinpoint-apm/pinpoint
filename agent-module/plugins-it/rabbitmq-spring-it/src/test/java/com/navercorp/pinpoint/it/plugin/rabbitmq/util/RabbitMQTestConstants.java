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

package com.navercorp.pinpoint.it.plugin.rabbitmq.util;

public class RabbitMQTestConstants {

    public static final String BROKER_HOST = "127.0.0.1";
    public static final String RABBITMQ_CLIENT = "RABBITMQ_CLIENT";
    public static final String RABBITMQ_CLIENT_INTERNAL = "RABBITMQ_CLIENT_INTERNAL";

    public static final String EXCHANGE = "TestExchange";
    public static final String QUEUE_PUSH = "TestPushQueue";
    public static final String QUEUE_DIRECT_PUSH = "TestDirectPushQueue";
    public static final String QUEUE_PULL = "TestPullQueue";
    public static final String ROUTING_KEY_PUSH = "push";
    public static final String ROUTING_KEY_DIRECT_PUSH = "direct-push";
    public static final String ROUTING_KEY_PULL = "pull";

    private RabbitMQTestConstants() {
    }
}
