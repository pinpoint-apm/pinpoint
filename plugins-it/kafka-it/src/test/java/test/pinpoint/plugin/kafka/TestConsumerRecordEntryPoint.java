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

package test.pinpoint.plugin.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

/**
 * @author Younsung Hwang
 */
public class TestConsumerRecordEntryPoint {

    public void consumeRecord(ConsumerRecord<String, String> record) {
        // cosume record ...
        // This method is called for kafka consumer invocatino. This method does nothing.
    }

    public void consumeRecord(ConsumerRecords<String, String> records) {
        // cosume records ...
        // This method is called for kafka consumer invocatino. This method does nothing.
    }

}
