/*
 * Copyright 2025 NAVER Corp.
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
package test.pinpoint.plugin.pulsar;

import java.util.UUID;

/**
 * @author zhouzixin@apache.org
 */
public class PulsarITConstants {

    public static final String TOPIC = "pulsar-it-topic";
    public static final String MESSAGE = "message-" + UUID.randomUUID();
    public static final String SUB_NAME = "pulsar-it–sub-name";
    public static final long MAX_TRACE_WAIT_TIME = 10000;
    public static final String PULSAR_CLIENT_SERVICE_TYPE = "PULSAR_CLIENT";
    public static final String PULSAR_CLIENT_INTERNAL_SERVICE_TYPE = "PULSAR_CLIENT_INTERNAL";
}
