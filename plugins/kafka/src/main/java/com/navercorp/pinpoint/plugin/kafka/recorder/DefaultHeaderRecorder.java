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
package com.navercorp.pinpoint.plugin.kafka.recorder;

import com.navercorp.pinpoint.bootstrap.context.AttributeRecorder;
import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaClientUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * @author yjqg6666
 */
public class DefaultHeaderRecorder implements HeaderRecorder {

    @Override
    public void record(AttributeRecorder recorder, ProducerRecord<?, ?> producerRecord) {
        if (!KafkaClientUtils.supportHeaders(producerRecord)) {
            return;
        }
        recordHeaders(recorder, producerRecord.headers());
    }

    @Override
    public void record(AttributeRecorder recorder, ConsumerRecord<?, ?> consumerRecord) {
        if (!KafkaClientUtils.supportHeaders(consumerRecord)) {
            return;
        }
        recordHeaders(recorder, consumerRecord.headers());
    }

    private void recordHeaders(AttributeRecorder recorder, org.apache.kafka.common.header.Headers headers) {
        if (recorder == null || headers == null) {
            return;
        }
        for (org.apache.kafka.common.header.Header header : headers) {
            final String key = header.key();
            final String val = BytesUtils.toString(header.value());

            if (!Header.startWithPinpointHeader(key)) {
                recorder.recordAttribute(KafkaConstants.KAFKA_HEADER_ANNOTATION_KEY, key + "=" + val);
            }
        }

    }
}
