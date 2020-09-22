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

package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.EndPointFieldAccessor;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.RemoteAddressFieldAccessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class ConsumerRecordsDesc {

    private final Set<String> topicSet;
    private final String endPointAddress;
    private final String remoteAddress;
    private final int size;

    private ConsumerRecordsDesc(Set<String> topicSet, String endPointAddress, String remoteAddress, int size) {
        this.topicSet = topicSet;
        this.endPointAddress = endPointAddress;
        this.remoteAddress = remoteAddress;
        this.size = size;
    }

    String getTopicString() {
        if (CollectionUtils.isEmpty(topicSet)) {
            return KafkaConstants.UNKNOWN;
        }

        if (CollectionUtils.nullSafeSize(topicSet) == 1) {
            return topicSet.iterator().next();
        }

        // [topica, topicb, topicc]
        return topicSet.toString();
    }

    String getEndPointAddress() {
        return endPointAddress;
    }

    String getRemoteAddress() {
        if (remoteAddress == null) {
            return KafkaConstants.UNKNOWN;
        } else {
            return remoteAddress;
        }
    }

    int size() {
        return size;
    }


    static ConsumerRecordsDesc create(Object object) {
        if (object instanceof Iterable) {
            return create(((Iterable) object).iterator());
        }

        return null;
    }

    static ConsumerRecordsDesc create(Iterator consumerRecordIterator) {
        Set<String> topicSet = new HashSet<String>(1);
        String remoteAddress = null;
        String endPointAddress  = null;
        int count = 0;

        while (consumerRecordIterator.hasNext()) {
            Object consumerRecord = consumerRecordIterator.next();
            if (consumerRecord instanceof ConsumerRecord) {
                if (StringUtils.isEmpty(remoteAddress)) {
                    remoteAddress = getRemoteAddress(consumerRecord);
                }
                if (StringUtils.isEmpty(endPointAddress)) {
                    endPointAddress = getEndPointAddress(consumerRecord);
                }

                String topic = ((ConsumerRecord) consumerRecord).topic();
                topicSet.add(topic);
                count++;
            }
        }

        if (count > 0) {
            return new ConsumerRecordsDesc(topicSet, endPointAddress, remoteAddress, count);
        }

        return null;
    }

    private static String getEndPointAddress(Object endPointFieldAccessor) {
        String endPointAddress = null;
        if (endPointFieldAccessor instanceof EndPointFieldAccessor) {
            endPointAddress = ((EndPointFieldAccessor) endPointFieldAccessor)._$PINPOINT$_getEndPoint();
        }

        return endPointAddress;
    }

    private static String getRemoteAddress(Object remoteAddressFieldAccessor) {
        if (remoteAddressFieldAccessor instanceof RemoteAddressFieldAccessor) {
            return ((RemoteAddressFieldAccessor) remoteAddressFieldAccessor)._$PINPOINT$_getRemoteAddress();
        }

        return null;
    }

}
