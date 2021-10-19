/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * @author yjqg6666
 */
public class KafkaClientUtils {

    private static final boolean clientSupportHeader;

    static {
        clientSupportHeader = clientSupportHeaders();
    }

    public static boolean isClientSupportHeader() {
        return clientSupportHeader;
    }

    private static boolean clientSupportHeaders() {
        try {
            ConsumerRecord.class.getMethod("headers");
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }


}
