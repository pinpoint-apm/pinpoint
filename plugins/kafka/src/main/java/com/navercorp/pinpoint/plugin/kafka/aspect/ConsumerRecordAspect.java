/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka.aspect;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.Aspect;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.JointPoint;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.PointCut;
import com.navercorp.pinpoint.plugin.kafka.encoder.PinpointValueEncoder;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Aspect
public abstract class ConsumerRecordAspect<K, V> extends ConsumerRecord {

    public ConsumerRecordAspect() {
        super("topic", 0, 0, null, null);
    }

    @PointCut
    public V value() {
        String transactionId = null, spanID = null, parentSpanID = null, parentApplicationName = null, parentApplicationType = null, flags = null;
        for (org.apache.kafka.common.header.Header header : headers().toArray()) {
            if (header.key().equals(Header.HTTP_TRACE_ID.toString())) {
                transactionId = new String(header.value());
            } else if (header.key().equals(Header.HTTP_PARENT_SPAN_ID.toString())) {
                parentSpanID = new String(header.value());
            } else if (header.key().equals(Header.HTTP_SPAN_ID.toString())) {
                spanID = new String(header.value());
            } else if (header.key().equals(Header.HTTP_PARENT_APPLICATION_NAME.toString())) {
                parentApplicationName = new String(header.value());
            } else if (header.key().equals(Header.HTTP_PARENT_APPLICATION_TYPE.toString())) {
                parentApplicationType = new String(header.value());
            } else if (header.key().equals(Header.HTTP_FLAGS.toString())) {
                flags = new String(header.value());
            }
        }

        if (transactionId != null && parentSpanID != null && spanID != null && parentApplicationName != null && parentApplicationType != null && flags != null) {
            return PinpointValueEncoder.INSTANCE.encode(__value(), transactionId, parentSpanID, spanID, parentApplicationName, parentApplicationType, flags);
        }
        return __value();
    }

    @JointPoint
    abstract V __value();
}
