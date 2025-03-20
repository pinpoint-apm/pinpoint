/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinpoint.test.plugin;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;

@Configuration
public class SpringKafkaErrorHandler implements CommonErrorHandler {

    @Override
    public boolean handleOne(Exception thrownException, ConsumerRecord<?, ?> record, Consumer<?, ?> consumer, MessageListenerContainer container) {
        System.out.println("Error occurred while processing: " + record);
        return true;
    }

    @Override
    public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer, MessageListenerContainer container, boolean batchListener) {
        System.out.println("Error occurred while processing: " + thrownException);
    }
}
