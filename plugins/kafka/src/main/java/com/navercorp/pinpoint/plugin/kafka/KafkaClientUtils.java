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
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * @author yjqg6666
 */
public class KafkaClientUtils {

    public static boolean supportHeaders(Class<?> clazz) {
        return clientSupportHeaders(clazz);
    }

    public static boolean supportHeaders(ConsumerRecord<?, ?> consumerRecord) {
        return consumerRecord != null && clientSupportHeaders(consumerRecord.getClass());
    }

    public static boolean supportHeaders(ProducerRecord<?, ?> producerRecord) {
        return producerRecord != null && clientSupportHeaders(producerRecord.getClass());
    }

    private static boolean clientSupportHeaders(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        try {
            clazz.getMethod("headers");
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }

}
