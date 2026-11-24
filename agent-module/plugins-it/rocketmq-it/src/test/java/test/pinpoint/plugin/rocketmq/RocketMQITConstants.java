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
package test.pinpoint.plugin.rocketmq;

public final class RocketMQITConstants {

    private RocketMQITConstants() {
    }

    public static final String NAMESRV_ADDR_PROPERTY = "ROCKETMQ_NAMESRV_ADDR";
    public static final String BROKER_ADDR_PROPERTY = "ROCKETMQ_BROKER_ADDR";

    public static final String ROCKETMQ_CLIENT_SERVICE_TYPE = "ROCKETMQ_CLIENT";
    public static final String ROCKETMQ_CLIENT_INTERNAL_SERVICE_TYPE = "ROCKETMQ_CLIENT_INTERNAL";

    // topic every broker owns when autoCreateTopicEnable=true; createTopic() uses it to find the brokers
    public static final String AUTO_CREATE_TOPIC_KEY = "TBW102";

    public static final long MAX_MESSAGE_WAIT_TIME = 60000;
    public static final long MAX_TRACE_WAIT_TIME = 10000;
}
